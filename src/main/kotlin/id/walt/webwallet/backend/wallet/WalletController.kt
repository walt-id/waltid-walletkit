package id.walt.webwallet.backend.wallet

import id.walt.model.DidMethod
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
    }

  fun listDids(ctx: Context) {
    ctx.json(DidService.listDids())
  }

  fun createDid(ctx:Context) {
    val method = DidMethod.valueOf(ctx.queryParam("method")!!)
    ctx.result(DidService.create(method))
  }
}