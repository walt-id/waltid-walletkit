package id.walt.issuer.backend

import com.nimbusds.oauth2.sdk.*
import com.nimbusds.oauth2.sdk.http.ServletUtils
import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import com.nimbusds.oauth2.sdk.token.RefreshToken
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata
import com.nimbusds.openid.connect.sdk.token.OIDCTokens
import id.walt.common.klaxonWithConverters
import id.walt.credentials.w3c.toVerifiableCredential
import id.walt.model.oidc.CredentialRequest
import id.walt.model.oidc.CredentialResponse
import id.walt.services.oidc.OIDC4CIService
import id.walt.verifier.backend.VerifierController
import id.walt.verifier.backend.WalletConfiguration
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.auth.UserInfo
import id.walt.webwallet.backend.context.WalletContextManager
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.*
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented
import java.net.URI

object IssuerController {
    val routes
        get() =
            path("") {
                path("wallets") {
                    get("list", documented(
                        document().operation {
                            it.summary("List wallet configurations")
                                .addTagsItem("Issuer")
                                .operationId("listWallets")
                        }
                            .jsonArray<WalletConfiguration>("200"),
                        VerifierController::listWallets,
                    ))
                }
                before("credentials/*") { WalletContextManager.setCurrentContext(IssuerManager.issuerContext) }
                after("credentials/*") { WalletContextManager.resetCurrentContext() }
                path("credentials") {
                    get("listIssuables", documented(
                        document().operation {
                            it.summary("List issuable credentials")
                                .addTagsItem("Issuer")
                                .operationId("listIssuableCredentials")
                        }
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
                                .queryParam<String>("walletId")
                                .queryParam<String>("sessionId")
                                .queryParam<Boolean>("isPreAuthorized")
                                .queryParam<String>("userPin")
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
                            .json<OIDCProviderMetadata>("200"),
                        IssuerController::oidcProviderMeta
                    ))
                    get(".well-known/openid-credential-issuer", documented(
                        document().operation {
                            it.summary("get OIDC provider meta data")
                                .addTagsItem("Issuer")
                                .operationId("oidcProviderMeta")
                        }
                            .json<OIDCProviderMetadata>("200"),
                        IssuerController::oidcProviderMeta
                    ))
                    post("par", documented(
                        document().operation {
                            it.summary("pushed authorization request")
                                .addTagsItem("Issuer")
                                .operationId("par")
                        }
                            .formParam<String>("response_type")
                            .formParam<String>("client_id")
                            .formParam<String>("redirect_uri")
                            .formParam<String>("scope")
                            .formParam<String>("claims")
                            .formParam<String>("state")
                            .formParam<String>("op_state")
                            .json<PushedAuthorizationSuccessResponse>("201"),
                        IssuerController::par
                    ))
                    get("fulfillPAR", documented(
                        document().operation { it.summary("fulfill PAR").addTagsItem("Issuer").operationId("fulfillPAR") }
                            .queryParam<String>("request_uri"),
                        IssuerController::fulfillPAR
                    ))
                    post("token", documented(
                        document().operation {
                            it.summary("token endpoint")
                                .addTagsItem("Issuer")
                                .operationId("token")
                        }
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
                            .body<CredentialRequest>()
                            .json<CredentialResponse>("200"),
                        IssuerController::credential
                    ))
                }
            }

    fun listIssuableCredentials(ctx: Context) {
        val sessionId = ctx.queryParam("sessionId")
        if (sessionId == null)
            ctx.json(IssuerManager.listIssuableCredentials())
        else
            ctx.json(IssuerManager.getIssuanceSession(sessionId)?.issuables ?: Issuables(credentials = listOf()))
    }

    fun requestIssuance(ctx: Context) {
        val wallet = ctx.queryParam("walletId")?.let { IssuerConfig.config.wallets.getOrDefault(it, null) }
            ?: IssuerManager.getXDeviceWallet()
        val session = ctx.queryParam("sessionId")?.let { IssuerManager.getIssuanceSession(it) }

        val selectedIssuables = ctx.bodyAsClass<Issuables>()
        if (selectedIssuables.credentials.isEmpty()) {
            ctx.status(HttpCode.BAD_REQUEST).result("No issuable credential selected")
            return
        }

        if (session != null) {
            val authRequest = session.authRequest ?: throw BadRequestResponse("No authorization request found for this session")
            IssuerManager.updateIssuanceSession(session, selectedIssuables)
            ctx.result("${authRequest.redirectionURI}?code=${IssuerManager.generateAuthorizationCodeFor(session)}&state=${authRequest.state.value}")
        } else {
            val userPin = ctx.queryParam("userPin")?.ifBlank { null }
            val isPreAuthorized = ctx.queryParam("isPreAuthorized")?.toBoolean() ?: false
            val initiationRequest =
                IssuerManager.newIssuanceInitiationRequest(selectedIssuables, isPreAuthorized, userPin)
            ctx.result("${wallet.url}/${wallet.receivePath}?${initiationRequest.toQueryString()}")
        }
    }

    fun oidcProviderMeta(ctx: Context) {
        ctx.json(IssuerManager.getOidcProviderMetadata().toJSONObject())
    }

    fun par(ctx: Context) {
        val req = AuthorizationRequest.parse(ServletUtils.createHTTPRequest(ctx.req))
        val session = if (req.customParameters.containsKey("op_state")) {
            IssuerManager.getIssuanceSession(req.customParameters["op_state"]!!.first())?.apply {
                authRequest = req
                IssuerManager.updateIssuanceSession(this, issuables)
            }
        } else {
            val authDetails = OIDC4CIService.getCredentialAuthorizationDetails(req)
            if (authDetails.isEmpty()) {
                ctx.status(HttpCode.BAD_REQUEST)
                    .json(
                        PushedAuthorizationErrorResponse(
                            ErrorObject(
                                "400",
                                "No credential authorization details given",
                                400
                            )
                        )
                    )
                return
            }
            IssuerManager.initializeIssuanceSession(authDetails, preAuthorized = false, req)
        } ?: throw BadRequestResponse("Session given by op_state not found")
        ctx.status(HttpCode.CREATED).json(
            PushedAuthorizationSuccessResponse(
                URI("urn:ietf:params:oauth:request_uri:${session.id}"),
                IssuerManager.EXPIRATION_TIME.seconds
            ).toJSONObject()
        )
    }

    fun fulfillPAR(ctx: Context) {
        val parURI = ctx.queryParam("request_uri")!!
        val sessionID = parURI.substringAfterLast("urn:ietf:params:oauth:request_uri:")
        val session = IssuerManager.getIssuanceSession(sessionID)
        if (session != null) {
            ctx.status(HttpCode.FOUND).header("Location", "${IssuerConfig.config.issuerUiUrl}/?sessionId=${session.id}")
        } else {
            ctx.status(HttpCode.FOUND)
                .header("Location", "${IssuerConfig.config.issuerUiUrl}/IssuanceError?message=Invalid issuance session")
        }
    }

    fun token(ctx: Context) {
        val tokenReq = TokenRequest.parse(ServletUtils.createHTTPRequest(ctx.req))
        val code = when (tokenReq.authorizationGrant.type) {
            GrantType.AUTHORIZATION_CODE -> (tokenReq.authorizationGrant as AuthorizationCodeGrant).authorizationCode
            PreAuthorizedCodeGrant.GRANT_TYPE -> (tokenReq.authorizationGrant as PreAuthorizedCodeGrant).code
            else -> throw BadRequestResponse("Unsupported grant type")
        }
        val sessionId = IssuerManager.validateAuthorizationCode(code.value)
        val session = IssuerManager.getIssuanceSession(sessionId)
        if (session == null) {
            ctx.status(HttpCode.NOT_FOUND).json(TokenErrorResponse(OAuth2Error.INVALID_REQUEST).toJSONObject())
            return
        }
        if (tokenReq.authorizationGrant.type == PreAuthorizedCodeGrant.GRANT_TYPE) {
            val pinMatches =
                session.userPin?.let { it == (tokenReq.authorizationGrant as PreAuthorizedCodeGrant).userPin } ?: true
            if (!pinMatches) {
                throw ForbiddenResponse("User PIN required")
            }
        }

        ctx.json(
            OIDCTokenResponse(
                OIDCTokens(JWTService.toJWT(UserInfo(session.id)), BearerAccessToken(session.id), RefreshToken()), mapOf(
                    "expires_in" to IssuerManager.EXPIRATION_TIME.seconds,
                    "c_nonce" to session.nonce
                )
            ).toJSONObject()
        )
    }

    fun credential(ctx: Context) {
        val session = ctx.header("Authorization")?.substringAfterLast("Bearer ")
            ?.let { IssuerManager.getIssuanceSession(it) }
            ?: throw ForbiddenResponse("Invalid or unknown access token")

        val credentialRequest =
            klaxonWithConverters.parse<CredentialRequest>(ctx.body()) ?: throw BadRequestResponse("Could not parse credential request body")

        val credential = IssuerManager.fulfillIssuanceSession(session, credentialRequest)
        if (credential.isNullOrEmpty()) {
            ctx.status(HttpCode.NOT_FOUND).result("No issuable credential with the given type found")
            return
        }
        val credObj = credential.toVerifiableCredential()
        ctx.contentType(ContentType.JSON).result(
            klaxonWithConverters.toJsonString(
                CredentialResponse(
                    if (credObj.jwt != null) "jwt_vc" else "ldp_vc",
                    credential.toVerifiableCredential()
                )
            )
        )
    }
}
