package id.walt.verifier.backend

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.nimbusds.jose.util.Base64URL
import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.nimbusds.oauth2.sdk.ResponseMode
import com.nimbusds.oauth2.sdk.id.State
import com.nimbusds.openid.connect.sdk.Nonce
import id.walt.auditor.*
import id.walt.model.dif.*
import id.walt.model.oidc.SIOPv2Response
import id.walt.multitenancy.TenantContext
import id.walt.multitenancy.TenantContextManager
import id.walt.multitenancy.TenantId
import id.walt.multitenancy.TenantType
import id.walt.servicematrix.BaseService
import id.walt.servicematrix.ServiceRegistry
import id.walt.services.oidc.OIDC4VPService
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.auth.UserInfo
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
import java.nio.ByteBuffer
import java.util.*

abstract class VerifierManager : BaseService() {
    open fun getVerifierContext(tenantId: String): TenantContext<VerifierConfig, VerifierState> {
        return TenantContextManager.getTenantContext(TenantId(TenantType.VERIFIER, tenantId)) { VerifierState() }
    }

    private val log = KotlinLogging.logger { }

    open fun newRequest(
        walletUrl: String,
        presentationDefinition: PresentationDefinition,
        state: String? = null,
        redirectCustomUrlQuery: String = "",
        responseMode: ResponseMode = ResponseMode.FORM_POST,
        presentationDefinitionByReference: Boolean = false,
        nonce: String? = null
    ): AuthorizationRequest {
        val nonce = nonce ?: Base64URL.encode(convertUUIDToBytes(UUID.randomUUID())).toString()
        val requestId = state ?: nonce
        val redirectQuery = if (redirectCustomUrlQuery.isEmpty()) "" else "?$redirectCustomUrlQuery"
        val req = OIDC4VPService.createOIDC4VPRequest(
            wallet_url = walletUrl,
            redirect_uri = URI.create("${VerifierTenant.config.verifierApiUrl}/verify$redirectQuery"),
            response_mode = responseMode,
            nonce = Nonce(nonce),
            presentation_definition = if (presentationDefinitionByReference) null else presentationDefinition,
            presentation_definition_uri = if (presentationDefinitionByReference)
                URI.create("${VerifierTenant.config.verifierApiUrl}/pd/$requestId").also {
                    VerifierTenant.state.presentationDefinitionCache.put(requestId, presentationDefinition)
                } else null,
            state = State(requestId)
        )
        VerifierTenant.state.reqCache.put(requestId, req)

        return req
    }

    private fun isWebhookAllowed(requestedWebhookUrl: String): Boolean {
        val allowedWebhooks = VerifierTenant.config.allowedWebhookHosts

        if (allowedWebhooks == null) {
            log.debug { "No allowedWebhookHosts attribute in verifier config, but webhook was requested." }
            return false
        }

        return allowedWebhooks.any { allowedUrl -> requestedWebhookUrl.startsWith(allowedUrl) }
    }

    fun newRequestBySchemaOrVc(
        walletUrl: String,
        schemaUris: Set<String>,
        vcTypes: Set<String>,
        state: String? = null,
        redirectCustomUrlQuery: String = "",
        responseMode: ResponseMode = ResponseMode.FORM_POST,
        verificationCallbackUrl: String? = null,
        presentationDefinitionByReference: Boolean = false,
        nonce: String? = null
    ): AuthorizationRequest {
        val request = when {
            schemaUris.isNotEmpty() -> newRequestBySchemaUris(
                walletUrl,
                schemaUris,
                state,
                redirectCustomUrlQuery,
                responseMode,
                presentationDefinitionByReference,
                nonce
            )

            else -> newRequestByVcTypes(
                walletUrl,
                vcTypes,
                state,
                redirectCustomUrlQuery,
                responseMode,
                presentationDefinitionByReference,
                nonce
            )
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
        walletUrl: String,
        schemaUris: Set<String>,
        state: String? = null,
        redirectCustomUrlQuery: String = "",
        responseMode: ResponseMode = ResponseMode.FORM_POST,
        presentationDefinitionByReference: Boolean = false,
        nonce: String? = null
    ): AuthorizationRequest {
        return newRequest(
            walletUrl, PresentationDefinition(
                id = "1",
                input_descriptors = schemaUris.mapIndexed { index, schemaUri ->
                    InputDescriptor(
                        id = "${index + 1}",
                        schema = VCSchema(uri = schemaUri)
                    )
                }.toList()
            ), state, redirectCustomUrlQuery, responseMode, presentationDefinitionByReference, nonce
        )
    }

    open fun newRequestByVcTypes(
        walletUrl: String,
        vcTypes: Set<String>,
        state: String? = null,
        redirectCustomUrlQuery: String = "",
        responseMode: ResponseMode = ResponseMode.FORM_POST,
        presentationDefinitionByReference: Boolean = false,
        nonce: String? = null
    ): AuthorizationRequest {
        return newRequest(
            walletUrl, PresentationDefinition(
                id = "1",
                input_descriptors = vcTypes.mapIndexed { index, vcType ->
                    InputDescriptor(
                        id = "${index + 1}",
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
            ), state, redirectCustomUrlQuery, responseMode, presentationDefinitionByReference, nonce
        )
    }

    open fun getVerificationPoliciesFor(req: AuthorizationRequest): List<VerificationPolicy> {
        log.info { "Getting verification policies for request: ${req.toURI()}" }
        return listOf(
            SignaturePolicy(),
            ChallengePolicy(req.getCustomParameter("nonce")!!.first(), applyToVC = false, applyToVP = true),
            PresentationDefinitionPolicy(
                VerifierTenant.state.presentationDefinitionCache.getIfPresent(req.state.value)
                    ?: OIDC4VPService.getPresentationDefinition(req)
            ),
            *(VerifierTenant.config.additionalPolicies?.map { p ->
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
        val req = VerifierTenant.state.reqCache.getIfPresent(state) ?: throw BadRequestResponse("State invalid or expired")
        VerifierTenant.state.reqCache.invalidate(state)
        val verifiablePresentations = siopResponse.vp_token

        val result = SIOPResponseVerificationResult(
            state = state,
            subject = verifiablePresentations.firstOrNull { !it.holder.isNullOrEmpty() }?.holder,
            vps = verifiablePresentations.map { vp ->
                VPVerificationResult(
                    vp = vp,
                    vcs = vp.verifiableCredential ?: listOf(),
                    verification_result = Auditor.getService().verify(
                        vp, getVerificationPoliciesFor(req)
                    )
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

        VerifierTenant.state.respCache.put(result.state, result)

        return Pair(result, callbackRequestedRedirectUrl)
    }

    open fun getVerificationRedirectionUri(
        verificationResult: SIOPResponseVerificationResult,
        uiUrl: String? = VerifierTenant.config.verifierUiUrl
    ): URI {
        return URI.create("$uiUrl/success/?access_token=${verificationResult.state}")
        /*if(verificationResult.isValid)
          return URI.create("$uiUrl/success/?access_token=${verificationResult.state}")
        else
          return URI.create("$uiUrl/error/?access_token=${verificationResult.state ?: ""}")

         */
    }

    fun getVerificationResult(id: String): SIOPResponseVerificationResult? {
        return VerifierTenant.state.respCache.getIfPresent(id).also {
            VerifierTenant.state.respCache.invalidate(id)
        }
    }

    override val implementation: BaseService
        get() = serviceImplementation<VerifierManager>()

    companion object {
        fun getService(): VerifierManager = ServiceRegistry.getService()

        fun convertUUIDToBytes(uuid: UUID): ByteArray? {
            val bb: ByteBuffer = ByteBuffer.wrap(ByteArray(16))
            bb.putLong(uuid.mostSignificantBits)
            bb.putLong(uuid.leastSignificantBits)
            return bb.array()
        }
    }
}

class DefaultVerifierManager : VerifierManager()
