package id.walt.verifier.backend

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.google.common.cache.CacheBuilder
import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.nimbusds.oauth2.sdk.ResponseMode
import com.nimbusds.oauth2.sdk.id.State
import com.nimbusds.openid.connect.sdk.Nonce
import id.walt.WALTID_DATA_ROOT
import id.walt.auditor.*
import id.walt.model.dif.*
import id.walt.model.oidc.SIOPv2Response
import id.walt.servicematrix.BaseService
import id.walt.servicematrix.ServiceRegistry
import id.walt.services.context.Context
import id.walt.services.context.ContextManager
import id.walt.services.hkvstore.FileSystemHKVStore
import id.walt.services.hkvstore.FilesystemStoreConfig
import id.walt.services.keystore.HKVKeyStoreService
import id.walt.services.oidc.OIDC4VPService
import id.walt.services.vcstore.HKVVcStoreService
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.auth.UserInfo
import id.walt.webwallet.backend.context.UserContext
import io.github.pavleprica.kotlin.cache.time.based.longTimeBasedCache
import io.javalin.http.BadRequestResponse
import io.javalin.http.UnauthorizedResponse
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.net.URI
import java.util.*
import java.util.concurrent.TimeUnit

abstract class VerifierManager : BaseService() {
    val reqCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build<String, AuthorizationRequest>()
    val respCache =
        CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build<String, SIOPResponseVerificationResult>()

    abstract val verifierContext: Context

    private val log = KotlinLogging.logger { }

    open fun newRequest(
        walletUrl: URI,
        presentationDefinition: PresentationDefinition,
        state: String? = null,
        redirectCustomUrlQuery: String = "",
        responseMode: ResponseMode = ResponseMode.FORM_POST
    ): AuthorizationRequest {
        val nonce = UUID.randomUUID().toString()
        val requestId = state ?: nonce
        val redirectQuery = if (redirectCustomUrlQuery.isEmpty()) "" else "?$redirectCustomUrlQuery"
        val req = OIDC4VPService.createOIDC4VPRequest(
            wallet_url = walletUrl,
            redirect_uri = URI.create("${VerifierConfig.config.verifierApiUrl}/verify$redirectQuery"),
            response_mode = responseMode,
            nonce = Nonce(nonce),
            presentation_definition = presentationDefinition,
            state = State(requestId)
        )
        reqCache.put(requestId, req)

        return req
    }

    private fun isWebhookAllowed(requestedWebhookUrl: String): Boolean {
        val allowedWebhooks = VerifierConfig.config.allowedWebhookHosts

        if (allowedWebhooks == null) {
            log.debug { "No allowedWebhookHosts attribute in verifier config, but webhook was requested." }
            return false
        }

        return allowedWebhooks.any { allowedUrl -> requestedWebhookUrl.startsWith(allowedUrl) }
    }

    fun newRequestBySchemaOrVc(
        walletUrl: URI,
        schemaUris: Set<String>,
        vcTypes: Set<String>,
        state: String? = null,
        redirectCustomUrlQuery: String = "",
        responseMode: ResponseMode = ResponseMode.FORM_POST,
        verificationCallbackUrl: String? = null
    ): AuthorizationRequest {
        val request = when {
            schemaUris.isNotEmpty() -> newRequestBySchemaUris(
                walletUrl,
                schemaUris,
                state,
                redirectCustomUrlQuery,
                responseMode
            )

            else -> newRequestByVcTypes(walletUrl, vcTypes, state, redirectCustomUrlQuery, responseMode)
        }

        if (verificationCallbackUrl != null) {
            log.debug { "Callback is set: $verificationCallbackUrl" }
            if (isWebhookAllowed(verificationCallbackUrl)) {
                log.debug { "Registered webhook for ${request.state.value}: $verificationCallbackUrl" }
                verificationCallbacks[request.state.value] = verificationCallbackUrl
            } else {
                throw UnauthorizedResponse("This web hook is not allowed.")
            }
        }

        return request
    }

    open fun newRequestBySchemaUris(
        walletUrl: URI,
        schemaUris: Set<String>,
        state: String? = null,
        redirectCustomUrlQuery: String = "",
        responseMode: ResponseMode = ResponseMode.FORM_POST
    ): AuthorizationRequest {
        return newRequest(
            walletUrl, PresentationDefinition(
                id = "1",
                input_descriptors = schemaUris.mapIndexed { index, schemaUri ->
                    InputDescriptor(
                        id = "${index+1}",
                        schema = VCSchema(uri = schemaUri)
                    )
                }.toList()
            ), state, redirectCustomUrlQuery, responseMode
        )
    }

    open fun newRequestByVcTypes(
        walletUrl: URI,
        vcTypes: Set<String>,
        state: String? = null,
        redirectCustomUrlQuery: String = "",
        responseMode: ResponseMode = ResponseMode.FORM_POST
    ): AuthorizationRequest {
        return newRequest(
            walletUrl, PresentationDefinition(
                id = "1",
                input_descriptors = vcTypes.mapIndexed { index, vcType ->
                    InputDescriptor(
                        id = "${index+1}",
                        constraints = InputDescriptorConstraints(
                            fields = listOf(
                                InputDescriptorField(
                                    path = listOf("$.type"),
                                    filter = mapOf(
                                        "const" to vcType
                                    )
                                )
                            )
                        )
                    )
                }.toList()
            ), state, redirectCustomUrlQuery, responseMode
        )
    }

    open fun getVerificationPoliciesFor(req: AuthorizationRequest): List<VerificationPolicy> {
        return listOf(
            SignaturePolicy(),
            ChallengePolicy(req.getCustomParameter("nonce")!!.first(), applyToVC = false, applyToVP = true),
            PresentationDefinitionPolicy(OIDC4VPService.getPresentationDefinition(req)),
            *(VerifierConfig.config.additionalPolicies?.map { p ->
                PolicyRegistry.getPolicyWithJsonArg(p.policy, p.argument?.let { JsonObject(it) })
            }?.toList() ?: listOf()).toTypedArray()
        )
    }

    private val verificationCallbackHttpClient = HttpClient(CIO) {
        //install(ContentNegotiation) {
        //jackson()
        //}
        install(Logging) {
            level = LogLevel.ALL
        }
        followRedirects = false
    }

    val verificationCallbacks = longTimeBasedCache<String, String>()

    /*
    - find cached request
    - parse and verify id_token
    - parse and verify vp_token
    -  - compare nonce (verification policy)
    -  - compare token_claim => token_ref => vp (verification policy)
     */
    open fun verifyResponse(siopResponse: SIOPv2Response): Pair<SIOPResponseVerificationResult, String?> {
        val state = siopResponse.state ?: throw BadRequestResponse("No state set on SIOP response")
        val req = reqCache.getIfPresent(state) ?: throw BadRequestResponse("State invalid or expired")
        reqCache.invalidate(state)
        val verifiablePresentations = siopResponse.vp_token

        val result = SIOPResponseVerificationResult(
            state = state,
            subject = verifiablePresentations.firstOrNull { !it.holder.isNullOrEmpty() }?.holder,
            vps = verifiablePresentations.map { vp ->
                VPVerificationResult(
                    vp = vp,
                    vcs = vp.verifiableCredential ?: listOf(),
                    verification_result = ContextManager.runWith(verifierContext) {
                        Auditor.getService().verify(
                            vp, getVerificationPoliciesFor(req)
                        )
                    }
                )
            }, auth_token = null
        )

        var overallValid = result.isValid

        // Verification callback
        val callbackRequestedRedirectUrl = if (verificationCallbacks[state].isPresent) {
            val callbackUrl = verificationCallbacks[state].get()
            log.debug { "Sending callback post to: \"$callbackUrl\" for state \"$state\"" }
            val response = runBlocking {
                verificationCallbackHttpClient.post(callbackUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(Klaxon().toJsonString(result))
                }
            }
            log.debug { "Callback response: $response" }

            when (response.status) {
                in setOf(HttpStatusCode.OK, HttpStatusCode.Accepted, HttpStatusCode.NoContent) -> {
                    verificationCallbacks.remove(state)
                    null
                }
                in setOf(
                    HttpStatusCode.MovedPermanently, HttpStatusCode.PermanentRedirect,
                    HttpStatusCode.Found, HttpStatusCode.SeeOther, HttpStatusCode.TemporaryRedirect
                ) -> response.headers[HttpHeaders.Location]
                in setOf(
                    HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden
                ) -> {
                    overallValid = false
                    null
                }
                else -> null
            }
        } else null

        if (result.isValid && overallValid) {
            result.auth_token = JWTService.toJWT(UserInfo(result.subject!!))
        }

        respCache.put(result.state, result)

        return Pair(result, callbackRequestedRedirectUrl)
    }

    open fun getVerificationRedirectionUri(
        verificationResult: SIOPResponseVerificationResult,
        uiUrl: String? = VerifierConfig.config.verifierUiUrl
    ): URI {
        return URI.create("$uiUrl/success/?access_token=${verificationResult.state}")
        /*if(verificationResult.isValid)
          return URI.create("$uiUrl/success/?access_token=${verificationResult.state}")
        else
          return URI.create("$uiUrl/error/?access_token=${verificationResult.state ?: ""}")

         */
    }

    fun getVerificationResult(id: String): SIOPResponseVerificationResult? {
        return respCache.getIfPresent(id).also {
            respCache.invalidate(id)
        }
    }

    override val implementation: BaseService
        get() = serviceImplementation<VerifierManager>()

    companion object {
        fun getService(): VerifierManager = ServiceRegistry.getService()
    }
}

class DefaultVerifierManager : VerifierManager() {
    override val verifierContext = UserContext(
        contextId = "Verifier",
        hkvStore = FileSystemHKVStore(FilesystemStoreConfig("$WALTID_DATA_ROOT/data/verifier")),
        keyStore = HKVKeyStoreService(),
        vcStore = HKVVcStoreService()
    )

}
