package id.walt.webwallet.backend.wallet

import com.nimbusds.oauth2.sdk.ResponseMode
import com.nimbusds.oauth2.sdk.util.URLUtils
import id.walt.BaseApiTest
import id.walt.auditor.PresentationDefinitionPolicy
import id.walt.common.KlaxonWithConverters
import id.walt.credentials.w3c.toVerifiableCredential
import id.walt.custodian.Custodian
import id.walt.issuer.backend.*
import id.walt.model.DidMethod
import id.walt.model.oidc.SIOPv2Response
import id.walt.multitenancy.TenantId
import id.walt.onboarding.backend.OnboardingController
import id.walt.services.context.ContextManager
import id.walt.services.did.DidService
import id.walt.services.hkvstore.InMemoryHKVStore
import id.walt.services.keystore.HKVKeyStoreService
import id.walt.services.oidc.OIDC4VPService
import id.walt.services.oidc.OidcSchemeFixer.unescapeOpenIdScheme
import id.walt.services.vcstore.HKVVcStoreService
import id.walt.signatory.ProofConfig
import id.walt.signatory.Signatory
import id.walt.verifier.backend.*
import id.walt.webwallet.backend.auth.AuthController
import id.walt.webwallet.backend.auth.UserInfo
import id.walt.webwallet.backend.config.WalletConfig
import id.walt.webwallet.backend.context.UserContext
import id.walt.webwallet.backend.context.UserContextLoader
import id.walt.webwallet.backend.context.WalletContextManager
import io.javalin.apibuilder.ApiBuilder
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.common.runBlocking
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.mockk.every
import io.mockk.mockkObject
import java.net.URI

class SIOPv2Test : BaseApiTest() {
    override fun loadRoutes() {
        ApiBuilder.path("api") {
            AuthController.routes
            WalletController.routes
            DidWebRegistryController.routes
        }
        ApiBuilder.path("verifier-api") {
            VerifierController.routes
        }
        ApiBuilder.path("issuer-api") {
            IssuerController.routes
        }
        ApiBuilder.path("onboarding-api") {
            OnboardingController.routes
        }
    }
    val clientNoFollow = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        expectSuccess = false
        followRedirects = false
    }


    @BeforeClass
    fun initSIOPTest() {
        mockkObject(VerifierTenant, IssuerTenant, WalletConfig, UserContextLoader)
        every { VerifierTenant.config } returns VerifierConfig(
            verifierUiUrl = "$url",
            verifierApiUrl = "$url/verifier-api/default",
            wallets = mapOf(
                "walt.id" to WalletConfiguration(
                    "walt.id", "$url",
                    "api/siop/initiatePresentation", "api/siop/initiateIssuance" , "walt.id web wallet"
                )
            )
        )
        every { WalletConfig.config } returns WalletConfig(
            walletUiUrl = "$url",
            walletApiUrl = "$url/api"
        )
        every { IssuerTenant.config } returns IssuerConfig(
            issuerUiUrl = "$url",
            issuerApiUrl = "$url/issuer-api/default"
        )
        every { UserContextLoader.load(any()) } returns UserContext("testuser",
            HKVKeyStoreService(),
            HKVVcStoreService(),
            InMemoryHKVStore()
            )
    }

    @Test
    fun test_OIDC4VP_vp_token_flow() = runBlocking {
        // VERIFIER PORTAL
        // verifier portal triggers presentation
        val redirectToWalletResponse = clientNoFollow.get("${VerifierTenant.config.verifierApiUrl}/present/?walletId=walt.id&vcType=VerifiableId") {}
        redirectToWalletResponse.status shouldBe HttpStatusCode.Found
        val redirectToWalletLocation = redirectToWalletResponse.headers.get("Location")!!
        val verificationReq = OIDC4VPService.parseOIDC4VPRequestUri(URI.create(redirectToWalletLocation))

        // WALLET
        // redirect to wallet api
        val redirectToWalletUiResponse = clientNoFollow.get(redirectToWalletLocation) {}
        redirectToWalletUiResponse.status shouldBe HttpStatusCode.Found
        val redirectToWalletUiLocation = redirectToWalletUiResponse.headers.get("Location")!!
        // parse redirection to wallet UI
        val sessionId = URLUtils.parseParameters(URI.create(redirectToWalletUiLocation).query).get("sessionId")!!.first()
        // simulate user auth
        val userInfo = authenticate()
        val did = ContextManager.runWith(WalletContextManager.getUserContext(userInfo)) {
            DidService.listDids().first()
        }
        val vc = ContextManager.runWith(WalletContextManager.getUserContext(userInfo)) {
            Signatory.getService().issue("VerifiableId", ProofConfig(did, did)).toVerifiableCredential().also {
                Custodian.getService().storeCredential(it.id!!, it)
            }
        }
        // wallet ui gets presentation session details
        val presentationSessionInfo = client.get("$url/api/wallet/presentation/continue?sessionId=$sessionId&did=$did") {
            header("Authorization", "Bearer ${userInfo.token}")
        }.bodyAsText().let {
            KlaxonWithConverters().parse<CredentialPresentationSessionInfo>(it)
        }
        presentationSessionInfo!!.id shouldBe sessionId
        presentationSessionInfo.did shouldBe did
        presentationSessionInfo.presentableCredentials shouldNotBe null
        val presentableCredentials = presentationSessionInfo.presentableCredentials!!.filter { c -> c.credentialId == vc.id }
        presentableCredentials.size shouldBe 1
        presentableCredentials.first().credentialId shouldBe vc.id
        presentationSessionInfo.redirectUri shouldBe verificationReq.redirectionURI.toString()

        // wallet ui confirms presentation request
        val presentationResponse = client.post("$url/api/wallet/presentation/fulfill?sessionId=$sessionId") {
            header("Authorization", "Bearer ${userInfo.token}")
            contentType(ContentType.Application.Json)
            setBody(KlaxonWithConverters().parseArray<Map<String, Any>?>(KlaxonWithConverters().toJsonString(presentableCredentials)))
        }.bodyAsText().let { KlaxonWithConverters().parse<PresentationResponse>(it) }

        presentationResponse!!.id_token shouldBe null
        presentationResponse.vp_token shouldNotBe null
        presentationResponse.presentation_submission shouldNotBe null
        presentationResponse.state shouldBe verificationReq.state.value

        // VERIFIER
        // receive presentation response
        val redirectToVerifierUIResponse = clientNoFollow.submitForm(presentationSessionInfo.redirectUri,
            Parameters.build {
                append("vp_token", presentationResponse.vp_token)
                append("presentation_submission", presentationResponse.presentation_submission)
                presentationResponse.state?.let { append("state", it) }
            }
        )

        redirectToVerifierUIResponse.status shouldBe HttpStatusCode.Found
        val redirectToVerifierUILocation = redirectToVerifierUIResponse.headers["Location"]
        val accessToken = URLUtils.parseParameters(URI.create(redirectToVerifierUILocation!!).query).get("access_token")!!.first()

        val verificationResult = client.get("${VerifierTenant.config.verifierApiUrl}/auth?access_token=$accessToken") {}.bodyAsText().let { KlaxonWithConverters().parse<SIOPResponseVerificationResult>(it) }
        verificationResult!!.isValid shouldBe true
        verificationResult.subject shouldBe did
        verificationResult.vps shouldHaveSize 1
        verificationResult.vps shouldHaveSize 1
        verificationResult.vps[0].vp.holder shouldBe did
        verificationResult.vps[0].vp.verifiableCredential shouldNotBe null
        verificationResult.vps[0].vp.verifiableCredential!! shouldHaveSize 1
        verificationResult.vps[0].vp.verifiableCredential!![0].id shouldBe vc.id
    }

    @Test
    fun testPreAuthzIssuanceFlow() {
        val preAuthReq = ContextManager.runWith(IssuerManager.getIssuerContext(TenantId.DEFAULT_TENANT)) {
            IssuerManager.newIssuanceInitiationRequest(Issuables(
                credentials = listOf(IssuableCredential("VerifiableId", null))
            ), preAuthorized = true)
        }
        val userInfo = UserInfo("testuser")
        val session = ContextManager.runWith(UserContextLoader.load(userInfo.id)) {
            val subjectDid = DidService.create(DidMethod.key)
            val sessionId = CredentialIssuanceManager.startIssuerInitiatedIssuance(preAuthReq)
            CredentialIssuanceManager.continueIssuerInitiatedIssuance(sessionId, subjectDid, userInfo, null)
        }
        session.credentials shouldNotBe null
        session.credentials!!.size shouldBe 1
        session.credentials!!.first().type shouldContain "VerifiableId"
    }

    @Test
    fun testPresentationDefinitionByReference() {
        val req = ContextManager.runWith(VerifierManager.getService().getVerifierContext(TenantId.DEFAULT_TENANT)) {
            // create req with pd by ref
            VerifierManager.getService().newRequestByVcTypes(
                "openid://",
                setOf("VerifiableId"),
                responseMode = ResponseMode("post"),
                presentationDefinitionByReference = true
            )
        }
        val reqUri = req.toURI().unescapeOpenIdScheme()

        // try to parse OIDC4VP request
        val parsedReq = shouldNotThrowAny {
            println("Parsing OIDC4VPRequestUri: $reqUri")
            OIDC4VPService.parseOIDC4VPRequestUri(reqUri)
        }

        // try to get presentation definition from URL:
        val pd = shouldNotThrowAny {
            OIDC4VPService.getPresentationDefinition(parsedReq)
        }
        pd.input_descriptors.flatMap { id -> id.constraints?.fields ?: listOf() }.firstOrNull { fd ->
            fd.path.contains("$.type") && fd.filter != null && fd.filter!!.containsKey("const") && fd.filter!!["const"] == "VerifiableId"
        } shouldNotBe null
    }

    @Test
    fun test__legacy_OIDC4VP_vp_token_flow() = runBlocking {
        // VERIFIER PORTAL
        // verifier portal triggers presentation
        val redirectToWalletResponse = clientNoFollow.get("${VerifierTenant.config.verifierApiUrl}/presentLegacy/?walletId=walt.id&vcType=VerifiableId") {}
        redirectToWalletResponse.status shouldBe HttpStatusCode.Found
        val redirectToWalletLocation = redirectToWalletResponse.headers.get("Location")!!
        val verificationReq = OIDC4VPService.parseOIDC4VPRequestUri(URI.create(redirectToWalletLocation))

        // WALLET
        // redirect to wallet api
        val redirectToWalletUiResponse = clientNoFollow.get(redirectToWalletLocation) {}
        redirectToWalletUiResponse.status shouldBe HttpStatusCode.Found
        val redirectToWalletUiLocation = redirectToWalletUiResponse.headers.get("Location")!!
        // parse redirection to wallet UI
        val sessionId = URLUtils.parseParameters(URI.create(redirectToWalletUiLocation).query).get("sessionId")!!.first()
        // simulate user auth
        val userInfo = authenticate()
        val did = ContextManager.runWith(WalletContextManager.getUserContext(userInfo)) {
            DidService.listDids().first()
        }
        val vc = ContextManager.runWith(WalletContextManager.getUserContext(userInfo)) {
            Signatory.getService().issue("VerifiableId", ProofConfig(did, did)).toVerifiableCredential().also {
                Custodian.getService().storeCredential(it.id!!, it)
            }
        }
        // wallet ui gets presentation session details
        val presentationSessionInfo = client.get("$url/api/wallet/presentation/continue?sessionId=$sessionId&did=$did") {
            header("Authorization", "Bearer ${userInfo.token}")
        }.bodyAsText().let {
            KlaxonWithConverters().parse<CredentialPresentationSessionInfo>(it)
        }
        presentationSessionInfo!!.id shouldBe sessionId
        presentationSessionInfo.did shouldBe did
        presentationSessionInfo.presentableCredentials shouldNotBe null
        val presentableCredentials = presentationSessionInfo.presentableCredentials!!.filter { c -> c.credentialId == vc.id }
        presentableCredentials.size shouldBe 1
        presentableCredentials.first().credentialId shouldBe vc.id
        presentationSessionInfo.redirectUri shouldBe verificationReq.redirectionURI.toString()

        // wallet ui confirms presentation request
        val presentationResponse = client.post("$url/api/wallet/presentation/fulfill?sessionId=$sessionId") {
            header("Authorization", "Bearer ${userInfo.token}")
            contentType(ContentType.Application.Json)
            setBody(KlaxonWithConverters().parseArray<Map<String, Any>?>(KlaxonWithConverters().toJsonString(presentableCredentials)))
        }.bodyAsText().let { KlaxonWithConverters().parse<PresentationResponse>(it) }

        presentationResponse!!.id_token shouldNotBe null
        presentationResponse.vp_token shouldNotBe null
        presentationResponse.presentation_submission shouldNotBe null
        presentationResponse.state shouldBe verificationReq.state.value

        // VERIFIER
        // receive presentation response
        val redirectToVerifierUIResponse = clientNoFollow.submitForm(presentationSessionInfo.redirectUri,
            Parameters.build {
                append("vp_token", presentationResponse.vp_token)
                append("id_token", presentationResponse.id_token!!)
                presentationResponse.state?.let { append("state", it) }
            }
        )

        redirectToVerifierUIResponse.status shouldBe HttpStatusCode.Found
        val redirectToVerifierUILocation = redirectToVerifierUIResponse.headers["Location"]
        val accessToken = URLUtils.parseParameters(URI.create(redirectToVerifierUILocation!!).query).get("access_token")!!.first()

        val verificationResult = client.get("${VerifierTenant.config.verifierApiUrl}/auth?access_token=$accessToken") {}.bodyAsText().let { KlaxonWithConverters().parse<SIOPResponseVerificationResult>(it) }
        verificationResult!!.isValid shouldBe true
        verificationResult.subject shouldBe did
        verificationResult.vps shouldHaveSize 1
        verificationResult.vps shouldHaveSize 1
        verificationResult.vps[0].vp.holder shouldBe did
        verificationResult.vps[0].vp.verifiableCredential shouldNotBe null
        verificationResult.vps[0].vp.verifiableCredential!! shouldHaveSize 1
        verificationResult.vps[0].vp.verifiableCredential!![0].id shouldBe vc.id
    }
}
