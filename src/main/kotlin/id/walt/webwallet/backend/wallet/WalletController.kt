package id.walt.webwallet.backend.wallet

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import id.walt.custodian.Custodian
import id.walt.model.DidMethod
import id.walt.model.IdToken
import id.walt.model.siopv2.*
import id.walt.rest.custodian.CustodianController
import id.walt.services.did.DidService
import id.walt.services.vc.VcUtils
import id.walt.vclib.Helpers.encode
import id.walt.vclib.Helpers.toCredential
import id.walt.vclib.model.VerifiableCredential
import id.walt.webwallet.backend.auth.UserRole
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.Context
import io.javalin.http.HttpCode
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented
import okhttp3.internal.wait
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

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
          get(
            documented(document().operation{
              it.summary("Create new DID").operationId("createDid").addTagsItem("wallet")
            }
              .queryParam<DidMethod>("method")
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
        // issuance
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
      }
    }

  fun listDids(ctx: Context) {
    ctx.json(DidService.listDids())
  }

  fun createDid(ctx:Context) {
    val method = DidMethod.valueOf(ctx.queryParam("method")!!)
    ctx.result(DidService.create(method))
  }

  fun getPresentationExchange(ctx: Context) {
    val req = SIOPv2Request.fromHttpContext(ctx)
    val did = ctx.queryParam("subject_did")!!
    ctx.json(PresentationExchange(did, req, getClaimedCredentials(did, req)))
  }

  private fun handlePresentationResponse(ctx: Context): Any? {
    val pe = ctx.bodyAsClass<PresentationExchange>()
    val myCredentials = Custodian.getService().listCredentials()
    val selectedCredentialIds = pe.claimedCredentials.map { cred -> cred.credentialId }.toSet()
    val selectedCredentials = myCredentials.filter { cred -> selectedCredentialIds.contains(cred.id) }.map { cred -> cred.encode() }.toList()
    val vp = Custodian.getService().createPresentation(selectedCredentials, pe.subject, null, challenge = pe.request.nonce)
    val id_token = SIOPv2IDToken(
      subject = pe.subject,
      client_id = pe.request.client_id,
      nonce = pe.request.nonce,
      vpTokenRef = VpTokenRef(
        presentation_submission = PresentationSubmission(
          id = "1",
          definition_id = "1",
          descriptor_map = listOf(
            PresentationDescriptor.fromVP("1", vp)
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
      val client = HttpClient.newBuilder().build();
      val request = HttpRequest.newBuilder()
        .uri(URI.create(pe.request.redirect_uri))
        .POST(formData(mapOf("id_token" to per.id_token, "vp_token" to per.vp_token)))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .build()
      val response = client.send(request, HttpResponse.BodyHandlers.ofString());
      //response.headers().map().forEach({ ctx.header(it.key, it.value.first())})
      val body = response.body()
      ctx.status(response.statusCode()).result(body)
      return body
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
      myCredentials.filter { it.type.contains(indesc.schema.uri.substringAfterLast("/")) &&
                              VcUtils.getSubject(it) == subject && !it.id.isNullOrEmpty() }.map { cred ->
        ClaimedCredential(indesc.id, cred.id!!)
      }
      }?.toList() ?: listOf()
  }

  private fun formData(data: Map<String, String>): HttpRequest.BodyPublisher? {

    val res = data.map {(k, v) -> "${(URLEncoder.encode(k, StandardCharsets.UTF_8))}=${URLEncoder.encode(v, StandardCharsets.UTF_8)}"}
      .joinToString("&")

    return HttpRequest.BodyPublishers.ofString(res)
  }





}