package id.walt.webwallet.backend.wallet

import id.walt.model.DidMethod
import id.walt.model.siopv2.SIOPv2Request
import id.walt.rest.custodian.CustodianController
import id.walt.services.did.DidService
import id.walt.webwallet.backend.auth.UserRole
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.Context
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
        get("parse", documented(
          document().operation {
            it.summary("Parse SIOPv2 request from URL query parameters")
              .operationId("parseSiopv2Request")
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
            .json<SIOPv2Request>("200"),
          WalletController::parseSiopv2Request
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

  fun parseSiopv2Request(ctx: Context) {
    ctx.json(SIOPv2Request.fromHttpContext(ctx))
  }
}