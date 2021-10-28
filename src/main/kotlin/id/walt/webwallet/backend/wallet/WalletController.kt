package id.walt.webwallet.backend.wallet

import id.walt.custodian.Custodian
import id.walt.model.DidMethod
import id.walt.model.IdToken
import id.walt.model.siopv2.*
import id.walt.rest.custodian.CustodianController
import id.walt.services.did.DidService
import id.walt.services.vc.VcUtils
import id.walt.vclib.Helpers.encode
import id.walt.webwallet.backend.auth.UserRole
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.Context
import io.javalin.http.HttpCode
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented

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

  fun postPresentationExchange(ctx: Context) {
    val pe = ctx.bodyAsClass<PresentationExchange>()
    val myCredentials = Custodian.getService().listCredentials()
    val selectedCredentialIds = pe.claimedCredentials.map { cred -> cred.credentialId }.toSet()
    val selectedCredentials = myCredentials.filter { cred -> selectedCredentialIds.contains(cred.id) }.map { cred -> cred.encode() }.toList()
    val vp = Custodian.getService().createPresentation(selectedCredentials, pe.subject, null, pe.request.nonce)
    val siopv2Response = SIOPv2Response(
      pe.subject,
      SIOPv2IDToken(
        subject = pe.subject,
        client_id = pe.request.client_id,
        nonce = pe.request.nonce
      ),
      SIOPv2VPToken(
        vp_token = listOf(
          SIOPv2Presentation.createFromVPString(vp)
        )
      )
    )
    ctx.json(PresentationExchangeResponse(
      id_token = siopv2Response.getIdToken(),
      vp_token = siopv2Response.getVpToken()
    ))
  }

  private fun getClaimedCredentials(subject: String, req: SIOPv2Request): List<ClaimedCredential> {
    val myCredentials = Custodian.getService().listCredentials()
    return req.claims.vp_token?.presentation_definition?.input_descriptors?.flatMap { indesc ->
      myCredentials.filter { it.type.contains(indesc.schema.substringAfterLast("/")) &&
                              VcUtils.getHolder(it) == subject && !it.id.isNullOrEmpty() }.map { cred ->
        ClaimedCredential(indesc.id, cred.id!!)
      }
      }?.toList() ?: listOf()
  }


}