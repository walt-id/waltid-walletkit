package id.walt.webwallet.backend.wallet

import com.beust.klaxon.Klaxon
import id.walt.custodian.CustodianService
import id.walt.model.DidMethod
import id.walt.model.siopv2.SIOPv2Request
import id.walt.servicematrix.ServiceMatrix
import id.walt.servicematrix.ServiceRegistry
import id.walt.services.context.WaltContext
import id.walt.services.did.DidService
import id.walt.signatory.ProofConfig
import id.walt.signatory.Signatory
import id.walt.vclib.Helpers.toCredential
import id.walt.vclib.model.VerifiableCredential
import id.walt.verifier.backend.SIOPv2RequestManager
import id.walt.webwallet.backend.auth.UserInfo
import id.walt.webwallet.backend.context.WalletContextManager
import io.javalin.http.Context
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SIOPv2Test : AnnotationSpec() {

  private lateinit var siopv2Request: SIOPv2Request
  private lateinit var subjectDid: String
  private lateinit var siopv2RequestParams: Map<String, String>
  private lateinit var vc1: VerifiableCredential
  private lateinit var vc2: VerifiableCredential

  @BeforeAll
  fun init() {
    ServiceMatrix("service-matrix.properties")
    ServiceRegistry.registerService<WaltContext>(WalletContextManager)
    WalletContextManager.setCurrentUserContext(UserInfo("test@mail.com"))

    siopv2Request = SIOPv2RequestManager.newRequest("https://www.w3.org/2018/credentials/v1/VerifiableId")
    subjectDid = DidService.create(DidMethod.ebsi)
    siopv2RequestParams = siopv2Request.toUriQueryString().split("&").map { it.split("=") }.map { it[0] to URLDecoder.decode(it[1], StandardCharsets.UTF_8) }.toMap()
    vc1 = Signatory.getService().issue("VerifiableId", ProofConfig(subjectDid, subjectDid)).toCredential()
    vc2 = Signatory.getService().issue("VerifiableId", ProofConfig(subjectDid, subjectDid)).toCredential()
    CustodianService.getService().storeCredential(vc1.id!!, vc1)
    CustodianService.getService().storeCredential(vc2.id!!, vc2)
  }

  @Test
  fun testPresentationExchange() {
    lateinit var pe: PresentationExchange
    val ctx1 = mockk<Context>(relaxed = true)
    every { ctx1.queryParam(not("subject_did")) } answers {  siopv2RequestParams.get(firstArg()) }
    every { ctx1.queryParam("subject_did") } returns subjectDid
    every { ctx1.json(ofType(PresentationExchange::class)) } answers { pe = firstArg(); ctx1 }

    WalletContextManager.setCurrentUserContext(UserInfo("test@mail.com"))
    WalletController.getPresentationExchange(ctx1)
    verify { ctx1.json(ofType(PresentationExchange::class)) }

    pe.subject shouldBe subjectDid
    pe.claimedCredentials shouldHaveSize 2
    vc1.id shouldBeIn pe.claimedCredentials.map { it.credential.id }
    vc2.id shouldBeIn pe.claimedCredentials.map { it.credential.id }

    /*val peSel = PresentationExchange(
      subjectDid, siopv2Request, listOf(pe.claimedCredentials[0])
    )
    lateinit var peResponse: PresentationExchangeResponse

    val ctx2 = mockk<Context>(relaxed = true)
    every { ctx2.bodyAsClass<PresentationExchange>() } returns peSel
    every { ctx2.json(ofType(PresentationExchangeResponse::class)) } answers { peResponse = firstArg(); ctx2 }

    WalletController.postPresentationExchange(ctx2)
    verify { ctx2.json(ofType(PresentationExchangeResponse::class)) }

    peResponse.id_token shouldNotBe null
    peResponse.vp_token shouldNotBe null*/
  }
}