package id.walt.webwallet.backend.wallet

import com.beust.klaxon.Klaxon
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTParser
import id.walt.custodian.Custodian
import id.walt.model.DidMethod
import id.walt.model.siopv2.SIOPv2IDToken
import id.walt.model.siopv2.SIOPv2Request
import id.walt.model.siopv2.SIOPv2VPToken
import id.walt.servicematrix.ServiceMatrix
import id.walt.servicematrix.ServiceRegistry
import id.walt.services.context.WaltContext
import id.walt.services.did.DidService
import id.walt.services.hkvstore.FileSystemHKVStore
import id.walt.services.hkvstore.FilesystemStoreConfig
import id.walt.services.jwt.JwtService
import id.walt.services.keystore.HKVKeyStoreService
import id.walt.services.vcstore.HKVVcStoreService
import id.walt.signatory.ProofConfig
import id.walt.signatory.Signatory
import id.walt.vclib.Helpers.toCredential
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.vclist.VerifiablePresentation
import id.walt.verifier.backend.SIOPv2RequestManager
import id.walt.webwallet.backend.auth.UserInfo
import id.walt.webwallet.backend.context.UserContext
import id.walt.webwallet.backend.context.WalletContextManager
import io.javalin.http.Context
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.beOfType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SIOPv2Test : AnnotationSpec() {

  val testUserContext = UserContext(HKVKeyStoreService(), HKVVcStoreService(), FileSystemHKVStore(FilesystemStoreConfig("data/test@mail.com")))
  val waltContext = mockk<WaltContext>(relaxed = true)
  private lateinit var siopv2Request: SIOPv2Request
  private lateinit var subjectDid: String
  private lateinit var siopv2RequestParams: Map<String, String>
  private lateinit var vc1: VerifiableCredential
  private lateinit var vc2: VerifiableCredential

  @BeforeAll
  fun init() {
    every { waltContext.getHKVStore() } returns testUserContext.hkvStore
    every { waltContext.getKeyStore() } returns testUserContext.keyStore
    every { waltContext.getVcStore() } returns testUserContext.vcStore

    ServiceMatrix("service-matrix.properties")
    ServiceRegistry.registerService<WaltContext>(waltContext)

    siopv2Request = SIOPv2RequestManager.newRequest("https://www.w3.org/2018/credentials/v1/VerifiableId")
    subjectDid = DidService.create(DidMethod.ebsi)
    siopv2RequestParams = siopv2Request.toUriQueryString().split("&").map { it.split("=") }.map { it[0] to URLDecoder.decode(it[1], StandardCharsets.UTF_8) }.toMap()
    vc1 = Signatory.getService().issue("VerifiableId", ProofConfig(subjectDid, subjectDid)).toCredential()
    vc2 = Signatory.getService().issue("VerifiableId", ProofConfig(subjectDid, subjectDid)).toCredential()
    Custodian.getService().storeCredential(vc1.id!!, vc1)
    Custodian.getService().storeCredential(vc2.id!!, vc2)
  }

  @Test()
  fun testPresentationExchange() {
    lateinit var pe: PresentationExchange
    val ctx1 = mockk<Context>(relaxed = true)
    every { ctx1.queryParam(not("subject_did")) } answers {  siopv2RequestParams.get(firstArg()) }
    every { ctx1.queryParam("subject_did") } returns subjectDid
    every { ctx1.json(ofType(PresentationExchange::class)) } answers { pe = firstArg(); ctx1 }

    WalletController.getPresentationExchange(ctx1)
    verify { ctx1.json(ofType(PresentationExchange::class)) }

    pe.subject shouldBe subjectDid
    pe.claimedCredentials shouldHaveSize 2
    vc1.id shouldBeIn pe.claimedCredentials.map { it.credentialId }
    vc2.id shouldBeIn pe.claimedCredentials.map { it.credentialId }

    val selectedCC = pe.claimedCredentials[0]
    val peSel = PresentationExchange(
      subjectDid, siopv2Request, listOf(selectedCC)
    )
    lateinit var peResponse: PresentationExchangeResponse

    val ctx2 = mockk<Context>(relaxed = true)
    every { ctx2.bodyAsClass<PresentationExchange>() } returns peSel
    every { ctx2.json(ofType(PresentationExchangeResponse::class)) } answers { peResponse = firstArg(); ctx2 }

    WalletController.postPresentationExchange(ctx2)
    verify { ctx2.json(ofType(PresentationExchangeResponse::class)) }

    peResponse.id_token shouldNotBe null
    peResponse.vp_token shouldNotBe null
    JwtService.getService().verify(peResponse.id_token) shouldBe true
    JwtService.getService().verify(peResponse.vp_token) shouldBe true

    val id_token = Klaxon().parse<SIOPv2IDToken>(JWTParser.parse(peResponse.id_token).jwtClaimsSet.toString())
    val vp_token = Klaxon().parse<SIOPv2VPToken>(JWTParser.parse(peResponse.vp_token).jwtClaimsSet.toString())

    id_token!!.subject shouldBe subjectDid
    vp_token!!.vp_token!! shouldHaveSize 1
    vp_token!!.vp_token!!.first().presentation.toCredential() should beOfType<VerifiablePresentation>()
    (vp_token!!.vp_token!!.first().presentation.toCredential() as VerifiablePresentation).verifiableCredential shouldHaveSize 1
    (vp_token!!.vp_token!!.first().presentation.toCredential() as VerifiablePresentation).verifiableCredential.first().id shouldBe selectedCC.credentialId

  }
}