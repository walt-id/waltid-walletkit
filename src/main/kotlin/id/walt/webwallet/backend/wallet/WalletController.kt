package id.walt.webwallet.backend.wallet

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import com.nimbusds.jose.util.Base64URL
import com.nimbusds.oauth2.sdk.http.HTTPRequest
import id.walt.crypto.KeyAlgorithm
import id.walt.custodian.Custodian
import id.walt.issuer.backend.IssuanceSession
import id.walt.model.DidMethod
import id.walt.model.DidUrl
import id.walt.model.dif.DescriptorMapping
import id.walt.model.dif.PresentationSubmission
import id.walt.model.oidc.IDToken
import id.walt.model.oidc.SIOPv2Request
import id.walt.model.oidc.VpTokenRef
import id.walt.model.oidc.klaxon
import id.walt.rest.custodian.CustodianController
import id.walt.services.context.ContextManager
import id.walt.services.did.DidService
import id.walt.services.essif.EssifClient
import id.walt.services.essif.didebsi.DidEbsiService
import id.walt.services.jwt.keyId
import id.walt.services.key.KeyService
import id.walt.vclib.model.toCredential
import id.walt.vclib.model.VerifiableCredential
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.auth.UserRole
import id.walt.webwallet.backend.config.WalletConfig
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.Context
import io.javalin.http.HttpCode
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.*

object WalletController {
  val routes
    get() = path("wallet") {
      path("did") {
        // list dids
        path("list") {
          get(
            documented(document().operation{
              it.summary("List my DIDs").operationId("listDids").addTagsItem("wallet")
            }
              .jsonArray<String>("200"),
              WalletController::listDids
            ), UserRole.AUTHORIZED
          )
        }
        // create new DID
        path("create") {
          post(
            documented(document().operation{
              it.summary("Create new DID").description("Creates and registers DID. For EBSI: needs bearer token to be given as form parameter 'ebsiBearerToken', for DID registration.").operationId("createDid").addTagsItem("wallet")
            }
              .queryParam<DidMethod>("method")
              .body<DidCreationRequest>()
              .result<String>("200"),
              WalletController::createDid
            ), UserRole.AUTHORIZED
          )
        }
      }
      path("credentials") {
        get("list", documented(CustodianController.listCredentialIdsDocs(), CustodianController::listCredentials), UserRole.AUTHORIZED)
      }
      path("siopv2") {
        get("presentationExchange", documented(
          document().operation {
            it.summary("Parse SIOPv2 request from URL query parameters")
              .operationId("getPresentationExchange")
              .addTagsItem("SIOPv2")
          }
            .queryParam<String>("response_type")
            .queryParam<String>("client_id")
            .queryParam<String>("redirect_uri")
            .queryParam<String>("scope")
            .queryParam<String>("nonce")
            .queryParam<String>("registration")
            .queryParam<Long>("exp")
            .queryParam<Long>("iat")
            .queryParam<String>("claims")
            .queryParam<String>("subject_did")
            .json<PresentationExchange>("200"),
          WalletController::getPresentationExchange
        ), UserRole.AUTHORIZED)
        post("presentationExchange", documented(
          document().operation {
            it.summary("Post presentation exchange with user selected credentials to be shared")
              .operationId("postPresentationExchange")
              .addTagsItem("SIOPv2")
          }
            .body<PresentationExchange>()
            .json<PresentationExchangeResponse>("200"),
          WalletController::postPresentationExchange
        ), UserRole.AUTHORIZED)
        // issuance // LEGACY
        get("credentialIssuance", documented(
          document().operation {
            it.summary("Parse SIOPv2 request from URL query parameters")
              .operationId("getCredentialIssuanceRequest")
              .addTagsItem("SIOPv2")
          }
            .queryParam<String>("response_type")
            .queryParam<String>("client_id")
            .queryParam<String>("redirect_uri")
            .queryParam<String>("scope")
            .queryParam<String>("nonce")
            .queryParam<String>("registration")
            .queryParam<Long>("exp")
            .queryParam<Long>("iat")
            .queryParam<String>("claims")
            .queryParam<String>("subject_did")
            .json<PresentationExchange>("200"),
          WalletController::getPresentationExchange // same as getPresentationExchange
        ), UserRole.AUTHORIZED)
        post("credentialIssuance", documented(
          document().operation {
            it.summary("Post presentation required by issuer, to issue credentials")
              .operationId("postCredentialIssuanceResponse")
              .addTagsItem("SIOPv2")
          }
            .body<PresentationExchange>()
            .json<PresentationExchangeResponse>("200"),
          WalletController::postCredentialIssuanceResponse
        ), UserRole.AUTHORIZED)
        // issuance // OIDC
        path("issuer") {
          get("list", documented(
            document().operation { it.summary("List known credential issuers").addTagsItem("SIOPv2").operationId("listIssuers") },
            WalletController::listIssuers),
            UserRole.UNAUTHORIZED
          )
          get("metadata", documented(
            document().operation { it.summary("get issuer meta data").addTagsItem("SIOPv2").operationId("issuerMeta") }
              .queryParam<String>("issuerId"),
            WalletController::issuerMeta),
            UserRole.UNAUTHORIZED)
        }
        post("initIssuance", documented(
          document().operation { it.summary("Initialize credential issuance from selected issuer").addTagsItem("SIOPv2").operationId("initIssuance") }
            .body<CredentialIssuanceRequest>()
            .result<String>("200"),
          WalletController::initIssuance
        ), UserRole.AUTHORIZED)
        get("finalizeIssuance", documented(
          document().operation { it.summary("Finalize credential issuance").addTagsItem("SIOPv2").operationId("finalizeIssuance") }
            .queryParam<String>("code")
            .queryParam<String>("state")
            .result<String>("302"),
          WalletController::finalizeIssuance
        ), UserRole.UNAUTHORIZED)
        get("issuedCredentials", documented(
          document().operation { it.summary("Get credentials issued in current issuance session").addTagsItem("SIOPv2").operationId("getIssuedCredentials") }
            .queryParam<String>("sessionId")
            .jsonArray<VerifiableCredential>("200"),
          WalletController::getIssuedCredentials
        ), UserRole.AUTHORIZED)
      }
    }

  fun listDids(ctx: Context) {
    ctx.json(DidService.listDids())
  }

  fun createDid(ctx:Context) {
    val method = DidMethod.valueOf(ctx.queryParam("method")!!)

    if(method == DidMethod.ebsi) {
      val didCreationReq = ctx.bodyAsClass<DidCreationRequest>()
      if(didCreationReq?.bearerToken.isNullOrEmpty()) {
        ctx.status(HttpCode.BAD_REQUEST).result("ebsiBearerToken form parameter is required for EBSI DID registration.")
        return
      }
      val key = KeyService.getService().listKeys().firstOrNull { k -> k.algorithm == KeyAlgorithm.ECDSA_Secp256k1 }?.keyId
                ?: KeyService.getService().generate(KeyAlgorithm.ECDSA_Secp256k1)
      val did = DidService.create(method, key.id)
      EssifClient.onboard(did, didCreationReq.bearerToken)
      EssifClient.authApi(did)
      DidEbsiService.getService().registerDid(did, did)
      ctx.result(did)
    } else if (method == DidMethod.key) {
      ctx.result(DidService.create(method, KeyService.getService().listKeys().firstOrNull { k -> k.algorithm == KeyAlgorithm.EdDSA_Ed25519 }?.keyId?.id))
    } else if (method == DidMethod.web) {
      val key = KeyService.getService().generate(KeyAlgorithm.RSA)
      val didStr = DidService.create(method, key.id, DidService.DidWebOptions(URI.create(WalletConfig.config.walletApiUrl).authority, "did-registry/${key.id}"))
      val didDoc = DidService.load(didStr)
      // !! Implicit USER CONTEXT is LOST after this statement !!
      ContextManager.runWith(DidWebRegistryController.didRegistryContext) {
        DidService.storeDid(didStr, didDoc.encodePretty())
      }
      ctx.result(didStr)
    }
  }

  fun getPresentationExchange(ctx: Context) {
    val req = SIOPv2Request.fromHttpContext(ctx)
    val did = ctx.queryParam("subject_did")!!
    ctx.json(PresentationExchange(did, req, getClaimedCredentials(did, req)))
  }

  private fun handlePresentationResponse(ctx: Context): Any? {
    val pe = ctx.body().let { klaxon.parse<PresentationExchange>(it) }!!
    val myCredentials = Custodian.getService().listCredentials()
    val selectedCredentialIds = pe.claimedCredentials.map { cred -> cred.credentialId }.toSet()
    val selectedCredentials = myCredentials.filter { cred -> selectedCredentialIds.contains(cred.id) }.map { cred -> cred.encode() }.toList()
    val vp = Custodian.getService().createPresentation(selectedCredentials, pe.subject, null, challenge = pe.request.nonce)
    val id_token = IDToken(
      subject = pe.subject,
      client_id = pe.request.client_id,
      nonce = pe.request.nonce ?: "",
      vpTokenRef = VpTokenRef(
        presentation_submission = PresentationSubmission(
          id = "1",
          definition_id = "1",
          descriptor_map = listOf(
            DescriptorMapping.fromVP("1", vp)
          )
        )
      )
    )
    val per = PresentationExchangeResponse(
      id_token = id_token.sign(),
      vp_token = vp
    )

    if(pe.request.response_mode == "form_post" || pe.request.response_mode == "fragment") {
      // trigger form post or call redirect uri with fragment from web UI in browser
      ctx.json(per)
      return per
    } else if(pe.request.response_mode == "post") {
      // post response to redirect uri and return result (avoiding CORS)
      val req = HTTPRequest(HTTPRequest.Method.POST, URI.create(pe.request.redirect_uri))
      req.query = formData(mapOf("id_token" to per.id_token, "vp_token" to per.vp_token))
      val response = req.send();
      ctx.status(response.statusCode).result(response.content)
      return response.content
    } else {
      ctx.status(HttpCode.BAD_REQUEST).result("Unknown response mode ${pe.request.response_mode}")
      return null
    }
  }

  fun postPresentationExchange(ctx: Context) {
    handlePresentationResponse(ctx)
  }

  val credentialConverter = object: Converter {
    override fun canConvert(cls: Class<*>)
        = cls == VerifiableCredential::class.java

    override fun toJson(value: Any): String
        = (value as VerifiableCredential).encode()

    override fun fromJson(jv: JsonValue)
        = jv.toString().toCredential()

  }

  fun postCredentialIssuanceResponse(ctx: Context) {
    val credentialsResponse = handlePresentationResponse(ctx)
    if(credentialsResponse == null) {
      return
    }
    val credentials = Klaxon().parseArray<VerifiableCredential>(credentialsResponse as String)
    credentials?.forEach {
      Custodian.getService().storeCredential(it.id!!, it)
    }
  }

  private fun getClaimedCredentials(subject: String, req: SIOPv2Request): List<ClaimedCredential> {
    val myCredentials = Custodian.getService().listCredentials()
    return req.claims.vp_token?.presentation_definition?.input_descriptors?.flatMap { indesc ->
      myCredentials.filter { indesc.schema.uri == it.credentialSchema?.id &&
                              it.subject == subject && !it.id.isNullOrEmpty() }.map { cred ->
        ClaimedCredential(indesc.id, cred.id!!)
      }
      }?.toList() ?: listOf()
  }

  private fun formData(data: Map<String, String>): String {

    return data.map {(k, v) -> "${(URLEncoder.encode(k, StandardCharsets.UTF_8))}=${URLEncoder.encode(v, StandardCharsets.UTF_8)}"}
      .joinToString("&")
  }

  fun listIssuers(ctx: Context) {
    ctx.json(WalletConfig.config.issuers.values)
  }

  fun issuerMeta(ctx: Context) {
    val metadata = WalletConfig.config.issuers.get(ctx.queryParam("issuerId"))?.metadata
    if(metadata != null)
      ctx.json(metadata.toJSONObject())
    else
      ctx.status(HttpCode.NOT_FOUND)
  }

  fun initIssuance(ctx: Context) {
    val issuance = ctx.bodyAsClass<CredentialIssuanceRequest>()
    val location = CredentialIssuanceManager.initIssuance(issuance, JWTService.getUserInfo(ctx)!!)
    if(!location.isNullOrEmpty()) {
      ctx.result(location)
    } else {
      ctx.status(HttpCode.INTERNAL_SERVER_ERROR)
    }
  }

  fun finalizeIssuance(ctx: Context) {
    val state = ctx.queryParam("state")
    val code = ctx.queryParam("code")
    if(state.isNullOrEmpty() || code.isNullOrEmpty()) {
      ctx.status(HttpCode.BAD_REQUEST).result("No state or authorization code given")
      return
    }
    val issuance = CredentialIssuanceManager.finalizeIssuance(state, code)
    if(issuance?.response != null) {
      ctx.status(HttpCode.FOUND).header("Location", "${WalletConfig.config.walletUiUrl}/ReceiveCredential?sessionId=${issuance.id}")
    } else {
      ctx.status(HttpCode.FOUND).header("Location", "${WalletConfig.config.walletUiUrl}/IssuanceError")
    }
  }

  fun getIssuedCredentials(ctx: Context) {
    val sessionId = ctx.queryParam("sessionId")
    val issuanceSession = sessionId?.let { CredentialIssuanceManager.getSession(it) }
    if(issuanceSession == null) {
      ctx.status(HttpCode.BAD_REQUEST).result("Invalid or expired session id given")
      return
    }
    ctx.json(issuanceSession.response?.map { it.credential }?.filterNotNull()?.map { String(Base64.getUrlDecoder().decode(it)).toCredential() } ?: listOf<VerifiableCredential>())
  }
}
