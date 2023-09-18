package id.walt.issuer.backend

import com.nimbusds.openid.connect.sdk.OIDCTokenResponse
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata
import id.walt.multitenancy.Tenant
import id.walt.multitenancy.TenantId
import id.walt.oid4vc.data.CredentialOffer
import id.walt.oid4vc.data.OfferedCredential
import id.walt.oid4vc.data.ResponseMode
import id.walt.oid4vc.errors.CredentialError
import id.walt.oid4vc.errors.TokenError
import id.walt.oid4vc.requests.AuthorizationRequest
import id.walt.oid4vc.requests.CredentialOfferRequest
import id.walt.oid4vc.requests.CredentialRequest
import id.walt.oid4vc.requests.TokenRequest
import id.walt.oid4vc.responses.PushedAuthorizationResponse
import id.walt.rest.core.DidController
import id.walt.rest.core.KeyController
import id.walt.signatory.rest.SignatoryController
import id.walt.verifier.backend.WalletConfiguration
import id.walt.webwallet.backend.context.WalletContextManager
import id.walt.webwallet.backend.wallet.DidCreationRequest
import id.walt.webwallet.backend.wallet.WalletController
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.*
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented
import kotlinx.serialization.json.JsonObject
import mu.KotlinLogging

object IssuerController {
    private val logger = KotlinLogging.logger { }
    val routes
        get() =
            path("{tenantId}") {
                before { ctx ->
                    logger.info { "Setting issuer API context: ${ctx.pathParam("tenantId")}" }
                    WalletContextManager.setCurrentContext(IssuerManager.getIssuerContext(ctx.pathParam("tenantId")))
                }
                after {
                    logger.info { "Resetting issuer API context" }
                    WalletContextManager.resetCurrentContext()
                }
                path("wallets") {
                    get("list", documented(
                        document().operation {
                            it.summary("List wallet configurations")
                                .addTagsItem("Issuer")
                                .operationId("listWallets")
                        }
                            .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                            .jsonArray<WalletConfiguration>("200"),
                        IssuerController::listWallets,
                    ))
                }
                path("config") {
                    fun OpenApiDocumentation.describeTenantId() =
                        this.run { pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) } }

                    path("did") {
                        post("create", documented(DidController.createDocs().describeTenantId(), DidController::create))
                        post("createAdvanced",
                            documented(document().operation {
                                it.summary("Create new DID")
                                    .description("Creates and registers a DID. Currently the DID methods: key, web, ebsi (v1/v2) and iota are supported. For EBSI v1: a  bearer token is required.")
                                    .operationId("createAdvanced").addTagsItem("Issuer Configuration")
                                    .addTagsItem("Decentralized Identifiers")
                            }
                                .body<DidCreationRequest>()
                                .result<String>("200"),
                                WalletController::createDid
                            )
                        )
                        post("import", documented(DidController.importDocs().describeTenantId(), DidController::import))
                        get("list", documented(DidController.listDocs().describeTenantId(), DidController::list))
                        post("delete", documented(DidController.deleteDocs().describeTenantId(), DidController::delete))
                    }

                    path("key") {
                        post("gen", documented(KeyController.genDocs().describeTenantId(), KeyController::gen))
                        post("import", documented(KeyController.importDocs().describeTenantId(), KeyController::import))
                        post("export", documented(KeyController.exportDocs().describeTenantId(), KeyController::export))
                        delete("delete", documented(KeyController.deleteDocs().describeTenantId(), KeyController::delete))
                        get("list", documented(KeyController.listDocs().describeTenantId(), KeyController::list))
                        post("load", documented(KeyController.loadDocs().describeTenantId(), KeyController::load))
                    }

                    post("setConfiguration", documented(document().operation {
                        it.summary("Set configuration for this issuer tenant").operationId("setConfiguration")
                            .addTagsItem("Issuer Configuration")
                    }
                        .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                        .body<IssuerConfig>()
                        .json<String>("200"), IssuerController::setConfiguration))
                    get("getConfiguration", documented(document().operation {
                        it.summary("Get configuration for this issuer tenant").operationId("getConfiguration")
                            .addTagsItem("Issuer Configuration")
                    }
                        .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                        .json<IssuerConfig>("200"), IssuerController::getConfiguration
                    ))
                    path("templates") {
                        get("", documented(document().operation {
                            it.summary("List templates").operationId("listTemplates").addTagsItem("Issuer Configuration")
                        }.json<Array<String>>("200"), SignatoryController::listTemplates))
                        get("{id}", documented(document().operation {
                            it.summary("Load a VC template").operationId("loadTemplate").addTagsItem("Issuer Configuration")
                        }.pathParam<String>("id") { it.description("Retrieves a single VC template form the data store") }
                            .json<String>("200"), SignatoryController::loadTemplate))
                        post(
                            "{id}", documented(
                                document().operation {
                                    it.summary("Import a VC template").operationId("importTemplate")
                                        .addTagsItem("Issuer Configuration")
                                }.pathParam<String>("id").body<String>(contentType = ContentType.JSON).result<String>("200"),
                                SignatoryController::importTemplate
                            )
                        )
                        delete("{id}", documented(document().operation {
                            it.summary("Remove VC template").operationId("removeTemplate").addTagsItem("Issuer Configuration")
                        }.pathParam<String>("id").result<String>("200"), SignatoryController::removeTemplate))

                    }
                }
                path("credentials") {
                    get("listIssuables", documented(
                        document().operation {
                            it.summary("List issuable credentials")
                                .addTagsItem("Issuer")
                                .operationId("listIssuableCredentials")
                        }
                            .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                            .queryParam<String>("sessionId")
                            .json<Issuables>("200"),
                        IssuerController::listIssuableCredentials))
                    path("issuance") {
                        post("request", documented(
                            document().operation {
                                it.summary("Request issuance of selected credentials to wallet")
                                    .addTagsItem("Issuer")
                                    .operationId("requestIssuance")
                            }
                                .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                                .queryParam<String>("walletId")
                                .queryParam<String>("sessionId")
                                .queryParam<Boolean>("isPreAuthorized")
                                .queryParam<String>("userPin")
                                .queryParam<String>("issuerDid")
                                .body<Issuables>()
                                .result<String>("200"),
                            IssuerController::requestIssuance
                        ))
                    }
                }
                path("oidc") {
                    get(".well-known/openid-configuration", documented(
                        document().operation {
                            it.summary("get OIDC provider meta data")
                                .addTagsItem("Issuer")
                                .operationId("oidcProviderMeta")
                        }
                            .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                            .json<OIDCProviderMetadata>("200"),
                        IssuerController::oidcProviderMeta
                    ))
                    get(".well-known/openid-credential-issuer", documented(
                        document().operation {
                            it.summary("get OIDC provider meta data")
                                .addTagsItem("Issuer")
                                .operationId("oidcProviderMeta")
                        }
                            .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                            .json<OIDCProviderMetadata>("200"),
                        IssuerController::oidcProviderMeta
                    ))
                    post("par", documented(
                        document().operation {
                            it.summary("pushed authorization request")
                                .addTagsItem("Issuer")
                                .operationId("par")
                        }
                            .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                            .formParam<String>("response_type")
                            .formParam<String>("client_id")
                            .formParam<String>("redirect_uri")
                            .formParam<String>("scope")
                            .formParam<String>("claims")
                            .formParam<String>("state")
                            .formParam<String>("op_state")
                            .json<PushedAuthorizationResponse>("201"),
                        IssuerController::par
                    ))
                    get("fulfillPAR", documented(
                        document().operation { it.summary("fulfill PAR").addTagsItem("Issuer").operationId("fulfillPAR") }
                            .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                            .queryParam<String>("request_uri"),
                        IssuerController::fulfillPAR
                    ))
                    post("token", documented(
                        document().operation {
                            it.summary("token endpoint")
                                .addTagsItem("Issuer")
                                .operationId("token")
                        }
                            .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                            .formParam<String>("grant_type")
                            .formParam<String>("code")
                            .formParam<String>("pre-authorized_code")
                            .formParam<String>("redirect_uri")
                            .formParam<String>("user_pin")
                            .formParam<String>("code_verifier")
                            .json<OIDCTokenResponse>("200"),
                        IssuerController::token
                    ))
                    post("credential", documented(
                        document().operation {
                            it.summary("Credential endpoint").operationId("credential").addTagsItem("Issuer")
                        }
                            .header<String>("Authorization")
                            .pathParam<String>("tenantId") { it.example(TenantId.DEFAULT_TENANT) }
                            .body<CredentialRequest>()
                            .json<String>("200"),
                        IssuerController::credential
                    ))
                }
            }

    private fun getConfiguration(context: Context) {
        try {
            context.json(IssuerTenant.config)
        } catch (nfe: Tenant.TenantNotFoundException) {
            throw NotFoundResponse()
        }
    }

    private fun setConfiguration(context: Context) {
        val config = context.bodyAsClass<IssuerConfig>()
        IssuerTenant.setConfig(config)
    }

    fun listWallets(ctx: Context) {
        ctx.json(IssuerTenant.config.wallets.values)
    }

    fun listIssuableCredentials(ctx: Context) {
        val sessionId = ctx.queryParam("sessionId")
        if (sessionId == null)
            ctx.json(IssuerManager.listIssuableCredentials())
        else
            ctx.json(IssuerManager.getSession(sessionId)?.credentialOffer?.credentials ?: Issuables(credentials = listOf()))
    }

    fun requestIssuance(ctx: Context) {
        val wallet = ctx.queryParam("walletId")?.let { IssuerTenant.config.wallets.getOrDefault(it, null) }
            ?: IssuerManager.getXDeviceWallet()
        val session = ctx.queryParam("sessionId")?.let { IssuerManager.getSession(it) }
        val issuerDid = ctx.queryParam("issuerDid") // OPTIONAL

        val selectedIssuables = ctx.bodyAsClass<Issuables>()
        if (selectedIssuables.credentials.isEmpty()) {
            ctx.status(HttpCode.BAD_REQUEST).result("No issuable credential selected")
            return
        }

        if (session != null) {
            val authRequest = session.authorizationRequest ?: throw BadRequestResponse("No authorization request found for this session")
            val codeResponse = IssuerManager.processCodeFlowAuthorization(authRequest)
            ctx.result(codeResponse.toRedirectUri(authRequest.redirectUri ?: "", authRequest.responseMode ?: ResponseMode.query))
        } else {
            val userPin = ctx.queryParam("userPin")?.ifBlank { null }
            val isPreAuthorized = ctx.queryParam("isPreAuthorized")?.toBoolean() ?: false
            val session =
                IssuerManager.initializeCredentialOffer(CredentialOffer.Builder(issuerDid ?: IssuerManager.defaultDid).apply {
                    selectedIssuables.credentials.forEach {
                        addOfferedCredential(OfferedCredential.fromJSON(it.credentialData ?: JsonObject(mapOf())))
                    }
                }, 0, isPreAuthorized, userPin)
            val credOfferReq = CredentialOfferRequest(session.credentialOffer)
            ctx.result("${wallet.url}${if (!wallet.url.endsWith("/")) "/" else ""}${wallet.receivePath}?${credOfferReq.toHttpQueryString()}")
        }
    }

    fun oidcProviderMeta(ctx: Context) {
        ctx.json(IssuerManager.metadata.toJSON())
    }

    fun par(ctx: Context) {
        val req = AuthorizationRequest.fromHttpParameters(ctx.req.parameterMap.mapValues { it.value.toList() })
        val session = if (req.customParameters.containsKey("op_state")) {
            IssuerManager.getSession(req.customParameters["op_state"]!!.first())?.apply {
                IssuerManager.putSession(this.id, this.copy(authorizationRequest = req))
            }
        } else {
            IssuerManager.initializeAuthorization(req, 600)
        } ?: throw BadRequestResponse("Session given by op_state not found")
        ctx.status(HttpCode.CREATED).json(
            IssuerManager.getPushedAuthorizationSuccessResponse(session).toJSON()
        )
    }

    fun fulfillPAR(ctx: Context) {
        val parURI = ctx.queryParam("request_uri")!!
        val sessionID = parURI.substringAfterLast("urn:ietf:params:oauth:request_uri:")
        val session = IssuerManager.getSession(sessionID)
        if (session != null) {
            ctx.status(HttpCode.FOUND).header("Location", "${IssuerTenant.config.issuerUiUrl}/?sessionId=${session.id}")
        } else {
            ctx.status(HttpCode.FOUND)
                .header("Location", "${IssuerTenant.config.issuerUiUrl}/IssuanceError?message=Invalid issuance session")
        }
    }

    fun token(ctx: Context) {
        val tokenReq = TokenRequest.fromHttpParameters(ctx.req.parameterMap.mapValues { it.value.toList() })
        try {
            val tokenResponse = IssuerManager.processTokenRequest(tokenReq)
            ctx.json(tokenResponse.toJSON())
        } catch (exc: TokenError) {
            ctx.status(HttpCode.BAD_REQUEST).json(exc.toAuthorizationErrorResponse().toJSON())
        }
    }

    fun credential(ctx: Context) {
        val accessToken = ctx.header("Authorization")?.substringAfterLast("Bearer ")
            ?: throw UnauthorizedResponse("No access token found")

        val credentialRequest = CredentialRequest.fromJSONString(ctx.body())

        try {
            ctx.json(IssuerManager.generateCredentialResponse(credentialRequest, accessToken).toJSON())
        } catch (exc: CredentialError) {
            throw BadRequestResponse(exc.toCredentialErrorResponse().error ?: "Bad request")
        }
    }
}
