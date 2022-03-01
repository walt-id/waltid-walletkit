package id.walt.onboarding.backend

import com.beust.klaxon.Klaxon
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.id.Issuer
import com.nimbusds.oauth2.sdk.token.AccessToken
import com.nimbusds.oauth2.sdk.token.AccessTokenType
import com.nimbusds.openid.connect.sdk.SubjectType
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata
import id.walt.auditor.Auditor
import id.walt.auditor.ChallengePolicy
import id.walt.auditor.SignaturePolicy
import id.walt.issuer.backend.IssuerConfig
import id.walt.issuer.backend.IssuerController
import id.walt.issuer.backend.IssuerManager
import id.walt.model.dif.*
import id.walt.services.context.ContextManager
import id.walt.services.context.WaltIdContextManager
import id.walt.services.oidc.OIDCUtils
import id.walt.vclib.model.AbstractVerifiableCredential
import id.walt.vclib.registry.VcTypeRegistry
import id.walt.vclib.templates.VcTemplateManager
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.auth.UserInfo
import io.javalin.apibuilder.ApiBuilder
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.*
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented
import java.net.URI

data class GenerateDomainVerificationCodeRequest(val domain: String)
data class CheckDomainVerificationCodeRequest(val domain: String)

object OnboardingController {
    val routes
        get() =
            path("") {
                path("domain") {
                    path("generateDomainVerificationCode") {
                        post("", documented(
                            document().operation {
                                it.summary("Generate domain verification code")
                                    .addTagsItem("Onboarding")
                                    .operationId("generateDomainVerificationCode")
                            }
                                .body<GenerateDomainVerificationCodeRequest>()
                                .result<String>("200"),
                            OnboardingController::generateDomainVerificationCode
                        ))
                    }
                    path("checkDomainVerificationCode") {
                        post("", documented(
                            document().operation {
                                it.summary("Check domain verification code")
                                    .addTagsItem("Onboarding")
                                    .operationId("checkDomainVerificationCode")
                            }
                                .body<CheckDomainVerificationCodeRequest>()
                                .result<Boolean>("200"),
                            OnboardingController::checkDomainVerificationCode
                        ))
                    }
                }
                // provide customized oidc discovery document and authorize endpoint
                path("oidc") {
                   get(".well-known/openid-configuration", documented(
                        document().operation {
                            it.summary("get OIDC provider meta data")
                                .addTagsItem("Onboarding")
                                .operationId("ob-oidcProviderMeta")
                        }
                            .json<OIDCProviderMetadata>("200"),
                        OnboardingController::oidcProviderMeta
                    ))
                    get("fulfillPAR", documented(
                        document().operation { it.summary("fulfill PAR").addTagsItem("Issuer").operationId("fulfillPAR") }
                            .queryParam<String>("request_uri"),
                        OnboardingController::fulfillPAR
                    ))
                }
                path("auth") {
                    get("userToken", documented(document().operation { it.summary("get user token") }.json<UserInfo>("200"),
                        OnboardingController::userToken))
                }
            }

    private fun generateDomainVerificationCode(ctx: Context) {
        val domainReq = ctx.bodyAsClass<GenerateDomainVerificationCodeRequest>()
        ctx.result(DomainOwnershipService.generateWaltIdDomainVerificationCode(domainReq.domain))
    }

    private fun checkDomainVerificationCode(ctx: Context) {
        val domainReq = ctx.bodyAsClass<CheckDomainVerificationCodeRequest>()
        ctx.json(DomainOwnershipService.checkWaltIdDomainVerificationCode(domainReq.domain))
    }

    const val PARICIPANT_CREDENTIAL_SCHEMA_ID = "https://raw.githubusercontent.com/walt-id/waltid-ssikit-vclib/master/src/test/resources/schemas/ParticipantCredential.json"

    private fun oidcProviderMeta(ctx: Context) {
        ctx.json(OIDCProviderMetadata(
            Issuer(IssuerConfig.config.onboardingApiUrl),
            listOf(SubjectType.PAIRWISE, SubjectType.PUBLIC),
            URI("http://blank")
        ).apply {
            authorizationEndpointURI = URI("${IssuerConfig.config.onboardingApiUrl}/oidc/fulfillPAR")
            pushedAuthorizationRequestEndpointURI = URI("${IssuerConfig.config.issuerApiUrl}/oidc/par") // keep issuer-api
            tokenEndpointURI = URI("${IssuerConfig.config.issuerApiUrl}/oidc/token") // keep issuer-api
            setCustomParameter("credential_endpoint", "${IssuerConfig.config.issuerApiUrl}/oidc/credential") // keep issuer-api
            setCustomParameter("nonce_endpoint", "${IssuerConfig.config.issuerApiUrl}/oidc/nonce") // keep issuer-api
            setCustomParameter("credential_manifests", listOf(
                CredentialManifest(
                    issuer = id.walt.model.dif.Issuer(IssuerManager.issuerDid, IssuerConfig.config.issuerClientName),
                    outputDescriptors = listOf(
                        OutputDescriptor(
                                    "ParticipantCredential",
                                PARICIPANT_CREDENTIAL_SCHEMA_ID,
                                "ParticipantCredential")
                    ),
                    presentationDefinition = PresentationDefinition(listOf()) // Request empty presentation to be sent along with issuance request
                )).map { net.minidev.json.parser.JSONParser().parse(Klaxon().toJsonString(it)) }
            )
        }.toJSONObject())
    }

    fun fulfillPAR(ctx: Context) {
        try {
            val parURI = ctx.queryParam("request_uri") ?: throw BadRequestResponse("no request_uri specified")
            val sessionID = parURI.substringAfterLast("urn:ietf:params:oauth:request_uri:")
            val session = IssuerManager.getIssuanceSession(sessionID)
                ?: throw BadRequestResponse("No session found for given sessionId, or session expired")
            // TODO: verify VP from auth request claims
            val vcclaims = OIDCUtils.getVCClaims(session.authRequest)
            val credClaim =
                vcclaims.credentials?.filter { cred -> cred.type == PARICIPANT_CREDENTIAL_SCHEMA_ID }?.firstOrNull()
                    ?: throw BadRequestResponse("No participant credential claim found in authorization request")
            val vp_token =
                credClaim.vp_token
                ?: session.authRequest.customParameters["vp_token"]?.flatMap {
                    OIDCUtils.fromVpToken(it) ?: listOf()
                } ?: listOf()
            val vp = vp_token.firstOrNull() ?: throw BadRequestResponse("No VP token found on authorization request")
            val subject = ContextManager.runWith(IssuerManager.issuerContext) {
                val verificationResult = Auditor.getService().verify(vp.encode(), listOf(SignaturePolicy(), ChallengePolicy(IssuerManager::checkNonce)))
                if(!verificationResult.valid) {
                    throw BadRequestResponse("Invalid VP token given, signature (${verificationResult.policyResults["SignaturePolicy"]}) and/or challenge (${verificationResult.policyResults["ChallengePolicy"]}) could not be verified")
                }
                vp.subject
            }

            session.did = subject
            IssuerManager.updateIssuanceSession(session, session.issuables)

            ctx.status(HttpCode.FOUND)
                .header("Location", "${IssuerConfig.config.onboardingUiUrl}?access_token=${session.id}")
        } catch (exc: Exception) {
            ctx.status(HttpCode.FOUND).header(
                "Location",
                "${IssuerConfig.config.issuerUiUrl}/IssuanceError/?message=${exc.message}"
            )
        }
    }

    fun userToken(ctx: Context) {
        val accessToken = ctx.header("Authorization")?.let { it.substringAfterLast("Bearer ") } ?: throw UnauthorizedResponse("No valid access token set on request")
        val session = IssuerManager.getIssuanceSession(accessToken) ?: throw UnauthorizedResponse("Invalid access token or session expired")
        val did = session.did ?: throw ForbiddenResponse("No DID specified on current session")

        val userInfo = UserInfo(did)
        ctx.json(userInfo.apply { token = JWTService.toJWT(userInfo) })
    }
}
