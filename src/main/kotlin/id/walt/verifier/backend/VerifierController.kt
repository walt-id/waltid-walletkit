package id.walt.verifier.backend

import com.nimbusds.jose.util.Base64URL
import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.nimbusds.oauth2.sdk.ResponseMode
import com.nimbusds.oauth2.sdk.ResponseType
import com.nimbusds.oauth2.sdk.Scope
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.id.State
import com.nimbusds.openid.connect.sdk.OIDCScopeValue
import id.walt.auditor.VerificationPolicy
import id.walt.auditor.dynamic.DynamicPolicyArg
import id.walt.common.KlaxonWithConverters
import id.walt.issuer.backend.IssuerConfig
import id.walt.model.dif.InputDescriptor
import id.walt.model.dif.InputDescriptorConstraints
import id.walt.model.dif.InputDescriptorField
import id.walt.model.dif.PresentationDefinition
import id.walt.model.oidc.SIOPv2Response
import id.walt.multitenancy.Tenant
import id.walt.multitenancy.TenantId
import id.walt.rest.auditor.AuditorRestController
import id.walt.services.oidc.OIDC4VPService
import id.walt.services.oidc.OidcSchemeFixer
import id.walt.services.oidc.OidcSchemeFixer.unescapeOpenIdScheme
import id.walt.verifier.backend.VerifierManager.Companion.convertUUIDToBytes
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.auth.UserRole
import id.walt.webwallet.backend.context.WalletContextManager
import io.github.pavleprica.kotlin.cache.time.based.customTimeBasedCache
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.*
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented
import mu.KotlinLogging
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

object VerifierController {

    private val log = KotlinLogging.logger { }

    val routes
        get() =
            path("{tenantId}") {
                before { ctx ->
                    //log.info { "Setting verifier API context: ${ctx.pathParam("tenantId")}" }
                    WalletContextManager.setCurrentContext(
                        VerifierManager.getService().getVerifierContext(ctx.pathParam("tenantId"))
                    )
                }
                after {
                    //log.info { "Resetting verifier API context" }
                    WalletContextManager.resetCurrentContext()
                }
                path("wallets") {
                    get("list", documented(
                        document().operation {
                            it.summary("List wallet configurations")
                                .addTagsItem("Verifier")
                                .operationId("listWallets")
                        }
                            .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                            .jsonArray<WalletConfiguration>("200"),
                        VerifierController::listWallets,
                    ))
                }
                path("present") {
                    get(documented(
                        document().operation {
                            it.summary("Present Verifiable ID")
                                .addTagsItem("Verifier")
                                .operationId("presentVID")
                        }
                            .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                            .queryParam<String>("walletId")
                            .queryParam<String>("schemaUri", isRepeatable = true)
                            .queryParam<String>("vcType", isRepeatable = true)
                            .queryParam<Boolean>("pdByReference") { it.description("true: include presentation definition by reference, else by value (default: false)") }
                            .result<String>("302"),
                        VerifierController::presentCredential
                    ))
                }
                path("presentXDevice") {
                    get(documented(
                        document().operation {
                            it.summary("Present Verifiable ID cross-device")
                                .addTagsItem("Verifier")
                                .operationId("presentXDevice")
                        }
                            .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                            .queryParam<String>("schemaUri", isRepeatable = true)
                            .queryParam<String>("vcType", isRepeatable = true)
                            .queryParam<Boolean>("pdByReference") { it.description("true: include presentation definition by reference, else by value (default: false)") }
                            .result<PresentationRequestInfo>("200"),
                        VerifierController::presentCredentialXDevice
                    ))
                }
                path("presentLegacy") {
                    get(documented(
                        document().operation {
                            it.summary("Present Verifiable ID cross-device in legacy format")
                                .addTagsItem("Verifier")
                                .operationId("presentLegacy")
                        }
                            .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                            .queryParam<String>("walletId")
                            .queryParam<String>("schemaUri", isRepeatable = true)
                            .queryParam<String>("vcType", isRepeatable = true)
                            .result<PresentationRequestInfo>("200"),
                        VerifierController::presentCredentialLegacy
                    ))
                }
                path("presentXDeviceLegacy") {
                    get(documented(
                        document().operation {
                            it.summary("Present Verifiable ID cross-device in legacy format")
                                .addTagsItem("Verifier")
                                .operationId("presentXDeviceLegacy")
                        }
                            .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                            .queryParam<String>("schemaUri", isRepeatable = true)
                            .queryParam<String>("vcType", isRepeatable = true)
                            .result<PresentationRequestInfo>("200"),
                        VerifierController::presentCredentialXDeviceLegacy
                    ))
                }
                get("pd/{id}", documented(document().operation {
                    it.summary("Get presentation definition from cache").operationId("pdFromCache").addTagsItem("Verifier")
                }
                    .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                    .pathParam<String>("id")
                    .json<PresentationDefinition>("200"),
                    VerifierController::getPresentationDefinitionFromCache))
                path("verify") {
                    post(documented(
                        document().operation {
                            it.summary("SIOPv2 request verification callback")
                                .addTagsItem("Verifier")
                                .operationId("verifySIOPv2Request")
                        }
                            .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                            .queryParam<String>("state")
                            .formParamBody<String> { }
                            .result<String>("302"),
                        VerifierController::verifySIOPResponse
                    ))
                    get("isVerified",
                        documented(
                            document().operation {
                                it.summary("SIOPv2 request verification callback receiver")
                                    .addTagsItem("Verifier")
                                    .operationId("isVerified")
                            }
                                .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                                .queryParam<String>("state")
                                .result<Unit>("404")
                                .result<String>("200"),
                            VerifierController::hasRecentlyVerified)
                    )
                }
                path("config") {
                    post("setConfiguration", documented(document().operation {
                        it.summary("Set configuration for this verifier tenant").operationId("setConfiguration")
                            .addTagsItem("Verifier Configuration")
                    }
                        .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                        .body<IssuerConfig>()
                        .json<String>("200"), VerifierController::setConfiguration))
                    get("getConfiguration", documented(document().operation {
                        it.summary("Get configuration for this verifier tenant").operationId("getConfiguration")
                            .addTagsItem("Verifier Configuration")
                    }
                        .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                        .json<IssuerConfig>("200"), VerifierController::getConfiguration
                    ))
                    path("policies") {
                        get(
                            "list",
                            documented(document().operation {
                                it.summary("List verification policies").operationId("listPolicies")
                                    .addTagsItem("Verifier Configuration")
                            }.pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                                .json<Array<VerificationPolicy>>("200"), AuditorRestController::listPolicies)
                        )
                        post(
                            "create/{name}",
                            documented(
                                document().operation {
                                    it.summary("Create dynamic verification policy").operationId("createDynamicPolicy")
                                        .addTagsItem("Verifier Configuration")
                                }
                                    .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                                    .pathParam<String>("name")
                                    .queryParam<Boolean>("update")
                                    .queryParam<Boolean>("downloadPolicy")
                                    .body<DynamicPolicyArg>()
                                    .json<DynamicPolicyArg>("200"),
                                AuditorRestController::createDynamicPolicy
                            )
                        )
                        delete(
                            "delete/{name}",
                            documented(
                                document().operation {
                                    it.summary("Delete a dynamic verification policy").operationId("deletePolicy")
                                        .addTagsItem("Verifier Configuration")
                                }.pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                                    .pathParam<String>("name"),
                                AuditorRestController::deleteDynamicPolicy
                            )
                        )
                    }
                }
                path("auth") {
                    get(documented(
                        document().operation {
                            it.summary("Complete authentication by siopv2 verification")
                                .addTagsItem("Verifier")
                                .operationId("completeAuthentication")
                        }
                            .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                            .queryParam<String>("access_token")
                            .json<Map<String, Any>>("200"),
                        VerifierController::completeAuthentication
                    ))
                }
                path("protected") {
                    get(documented(
                        document().operation {
                            it.summary("Fetch protected data (example)")
                                .addTagsItem("Verifier")
                                .operationId("get protected data")
                        }
                            .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                            .result<String>("200"),
                        VerifierController::getProtectedData
                    ), UserRole.AUTHORIZED)
                }
            }

    private fun getPresentationDefinitionFromCache(context: Context) {
        val id = context.pathParam("id")
        val pd = VerifierTenant.state.presentationDefinitionCache.getIfPresent(id)
            ?: throw BadRequestResponse("Presentation definition id invalid or expired")
        context.contentType(ContentType.APPLICATION_JSON).result(KlaxonWithConverters().toJsonString(pd))
    }

    private fun getConfiguration(context: Context) {
        try {
            context.json(VerifierTenant.config)
        } catch (nfe: Tenant.TenantNotFoundException) {
            throw NotFoundResponse()
        }
    }

    private fun setConfiguration(context: Context) {
        val config = context.bodyAsClass<VerifierConfig>()
        VerifierTenant.setConfig(config)
    }

    fun listWallets(ctx: Context) {
        ctx.json(VerifierTenant.config.wallets.values)
    }

    private fun getPresentationCustomQueryParams(queryParamMap: Map<String, List<String>>): String {
        val standardQueryParams = listOf("walletId", "schemaUri", "vcType", "verificationCallbackUrl", "pdByReference")

        val customQueryParams = queryParamMap
            .filter { it.key !in standardQueryParams }
            .flatMap { entry ->
                entry.value.map { value -> "${entry.key}=${URLEncoder.encode(value, StandardCharsets.UTF_8)}" }
            }.joinToString("&")

        return customQueryParams
    }

    private fun Context.getSchemaOrVcType(): Triple<List<String>, List<String>, String?> {
        val schemaUris = queryParams("schemaUri")
        val vcTypes = queryParams("vcType")
        if (schemaUris.isEmpty() && vcTypes.isEmpty()) {
            throw BadRequestResponse("No schema URI(s) or VC type(s) given")
        }
        val verificationCallbackUrl: String? = queryParam("verificationCallbackUrl")

        return Triple(schemaUris, vcTypes, verificationCallbackUrl)
    }

    val verifierManager = VerifierManager.getService()

    fun presentCredential(ctx: Context) {
        val wallet = ctx.queryParam("walletId")?.let { VerifierTenant.config.wallets[it] }
            ?: throw BadRequestResponse("Unknown or missing walletId")

        val (schemaUris, vcTypes, verificationCallbackUrl) = ctx.getSchemaOrVcType()
        log.debug { "Found requested callback: $verificationCallbackUrl" }

        val customQueryParams = getPresentationCustomQueryParams(ctx.queryParamMap())

        val req = verifierManager.newRequestBySchemaOrVc(
            walletUrl = "${wallet.url}/${wallet.presentPath}",
            schemaUris = schemaUris.toSet(),
            vcTypes = vcTypes.toSet(),
            redirectCustomUrlQuery = customQueryParams,
            responseMode = ResponseMode.FORM_POST,
            verificationCallbackUrl = verificationCallbackUrl,
            presentationDefinitionByReference = ctx.queryParam("pdByReference")?.toBoolean() ?: false
        )

        ctx.status(HttpCode.FOUND).header("Location", req.toURI().unescapeOpenIdScheme().toString())
    }

    fun presentCredentialLegacy(ctx: Context) {
        val wallet = ctx.queryParam("walletId")?.let { VerifierTenant.config.wallets[it] }
            ?: throw BadRequestResponse("Unknown or missing walletId")

        val (_, vcTypes, verificationCallbackUrl) = ctx.getSchemaOrVcType()
        log.debug { "Found requested callback: $verificationCallbackUrl" }

        val walletUrl = URI.create("${wallet.url}/${wallet.presentPath}")
        val redirectUri = URI.create("${VerifierTenant.config.verifierApiUrl}/verify")

        val nonce = Base64URL.encode(convertUUIDToBytes(UUID.randomUUID())).toString()
        val customParams = mutableMapOf("nonce" to listOf(nonce))

        val presentation_definition = PresentationDefinition(
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
        )

        //val presentationDefinitionKey = "presentation_definition"
        //val presentationDefinitionValue = KlaxonWithConverters().toJsonString(presentation_definition)
        //customParams[presentationDefinitionKey] = listOf(presentationDefinitionValue)

        customParams["claims"] = listOf(
            KlaxonWithConverters().toJsonString(
                mapOf(
                    //"id_token" to "",
                    "vp_token" to mapOf(
                        "presentation_definition" to presentation_definition
                    )
                )
            )
        )
        val req2 = AuthorizationRequest(
            walletUrl,
            ResponseType("id_token"),
            ResponseMode("form_post"),
            ClientID(redirectUri.toString()),
            redirectUri, Scope(OIDCScopeValue.OPENID),
            State(nonce),
            null, null, null, false, null, null, null,
            customParams
        )
        VerifierTenant.state.reqCache.put(nonce, OIDC4VPService.parseOIDC4VPRequestUri(req2.toURI()))

        ctx.status(HttpCode.FOUND).header("Location", req2.toURI().toString())
    }

    fun presentCredentialXDevice(ctx: Context) {
        val (schemaUris, vcTypes, verificationCallbackUrl) = ctx.getSchemaOrVcType()

        val customQueryParams = getPresentationCustomQueryParams(ctx.queryParamMap())

        val req = verifierManager.newRequestBySchemaOrVc(
            walletUrl = "openid://",
            schemaUris = schemaUris.toSet(),
            vcTypes = vcTypes.toSet(),
            redirectCustomUrlQuery = customQueryParams,
            responseMode = ResponseMode("post"),
            verificationCallbackUrl = verificationCallbackUrl,
            presentationDefinitionByReference = ctx.queryParam("pdByReference")?.toBoolean() ?: false
        )

        ctx.json(PresentationRequestInfo(req.state.value, req.toURI().unescapeOpenIdScheme().toString()))
    }


    fun presentCredentialXDeviceLegacy(ctx: Context) {

        val (_, vcTypes, verificationCallbackUrl) = ctx.getSchemaOrVcType()

        log.debug { "Found requested callback: $verificationCallbackUrl" }

        val redirectUri = URI.create("${VerifierTenant.config.verifierApiUrl}/verify")

        val nonce = Base64URL.encode(convertUUIDToBytes(UUID.randomUUID())).toString()
        val customParams = mutableMapOf("nonce" to listOf(nonce))

        val presentation_definition = PresentationDefinition(
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
        )

        customParams["claims"] = listOf(
            KlaxonWithConverters().toJsonString(
                mapOf(
                    //"id_token" to "",
                    "vp_token" to mapOf(
                        "presentation_definition" to presentation_definition
                    )
                )
            )
        )

        val req2 = AuthorizationRequest(
            OidcSchemeFixer.openIdSchemeFixUri,
            ResponseType("id_token"),
            ResponseMode("post"),
            ClientID(redirectUri.toString()),
            redirectUri, Scope(OIDCScopeValue.OPENID),
            State(nonce),
            null, null, null, false, null, null, null,
            customParams
        )

        VerifierTenant.state.reqCache.put(nonce, OIDC4VPService.parseOIDC4VPRequestUri(req2.toURI()))

        ctx.json(PresentationRequestInfo(req2.state.value, req2.toURI().unescapeOpenIdScheme().toString()))
    }

    val recentlyVerifiedResponses = customTimeBasedCache<String, String>(java.time.Duration.ofSeconds(300))

    fun hasRecentlyVerified(ctx: Context) {
        val accessToken = ctx.queryParam("state").toString()
        val opt = recentlyVerifiedResponses[accessToken]

        when {
            opt.isPresent -> ctx.status(200).result(opt.get())
            opt.isEmpty -> ctx.status(404)
        }
    }

    fun verifySIOPResponse(ctx: Context) {
        log.debug { "Verifying SIOP response..." }
        val verifierUiUrl = ctx.queryParam("verifierUiUrl") ?: VerifierTenant.config.verifierUiUrl
        val siopResponse =
            SIOPv2Response.fromFormParams(ctx.formParamMap().map { kv -> Pair(kv.key, kv.value.first()) }.toMap())

        val result = verifierManager.verifyResponse(siopResponse)
        val siopVerificationResult = result.first
        val callbackRequestedRedirectUrl = result.second

        val accessToken = siopResponse.state!!

        log.debug { "$accessToken: SIOP requests response: $siopVerificationResult" }
        log.debug { "$accessToken: The UI URL $verifierUiUrl has run through the verification process." }
        log.debug { "$accessToken: Callback?: $callbackRequestedRedirectUrl" }

        val url = if (callbackRequestedRedirectUrl != null) {
            callbackRequestedRedirectUrl
        } else {
            val url = verifierManager.getVerificationRedirectionUri(siopVerificationResult, verifierUiUrl).toString()

            log.debug { "Setting recentlyVerifiedResponses for \"$accessToken\" to redirect to \"$url\"" }
            recentlyVerifiedResponses[accessToken] = url

            url
        }
        log.debug { "Now $accessToken will be redirected to $url!" }

        ctx.status(HttpCode.FOUND).header("Location", url)
    }

    fun completeAuthentication(ctx: Context) {
        val access_token = ctx.queryParam("access_token")
        if (access_token == null) {
            ctx.status(HttpCode.FORBIDDEN).result("Unknown access token.")
            return
        }
        val result = VerifierManager.getService().getVerificationResult(access_token)
        if (result == null) {
            ctx.status(HttpCode.FORBIDDEN).result("Invalid SIOP Response Verification Result.")
            return
        }
        ctx.contentType(ContentType.JSON).result(KlaxonWithConverters().toJsonString(result))
    }

    fun getProtectedData(ctx: Context) {
        val userInfo = JWTService.getUserInfo(ctx)
        if (userInfo != null) {
            ctx.result("Account balance: EUR 0.00")
        } else {
            ctx.status(HttpCode.FORBIDDEN)
        }
    }
}
