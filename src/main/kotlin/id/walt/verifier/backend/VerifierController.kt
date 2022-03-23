package id.walt.verifier.backend
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.auth.UserRole
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.HttpCode
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented

object VerifierController {
  val routes
    get() =
      path("") {
        path("wallets") {
          get("list", documented(
            document().operation {
              it.summary("List wallet configurations")
                .addTagsItem("Verifier")
                .operationId("listWallets")
            }
              .jsonArray<WalletConfiguration>("200"),
            VerifierController::listWallets,
          ))
        }
        path("present") {
          get(documented(
            document().operation {
              it.summary("Present Verifiable ID")
                .addTagsItem("Verifier")
                .operationId("presentVID")
            }
              .queryParam<String>("walletId")
              .queryParam<String>("schemaUri")
              .result<String>("302"),
            VerifierController::presentCredential
          ))
        }
        path("verify") {
          post( documented(
            document().operation {
              it.summary("SIOPv2 request verification callback")
                .addTagsItem("Verifier")
                .operationId("verifySIOPv2Request")
            }
              .queryParam<String>("state")
              .formParamBody<String> { }
              .result<String>("302"),
            VerifierController::verifySIOPResponse
          ))
        }
        path("auth") {
          get(documented(
            document().operation {
              it.summary("Complete authentication by siopv2 verification")
                .addTagsItem("Verifier")
                .operationId("completeAuthentication")
            }
              .queryParam<String>("access_token")
              .json<SIOPResponseVerificationResult>("200"),
            VerifierController::completeAuthentication
          ))
        }
        path("protected") {
          get(documented(
            document().operation {
              it.summary("Fetch protected data (example)")
                .addTagsItem("Verifier")
                .operationId("get protected data")
            }
              .result<String>("200"),
            VerifierController::getProtectedData
          ), UserRole.AUTHORIZED)
        }
      }

  fun listWallets(ctx: Context) {
    ctx.json(VerifierConfig.config.wallets.values)
  }

  fun presentCredential(ctx: Context) {
    val walletId = ctx.queryParam("walletId")
    val schemaUri = ctx.queryParam("schemaUri")
    if(walletId.isNullOrEmpty() || !VerifierConfig.config.wallets.contains(walletId)) {
      ctx.status(HttpCode.BAD_REQUEST).result("Unknown wallet ID given")
    } else if(schemaUri.isNullOrEmpty()) {
      ctx.status(HttpCode.BAD_REQUEST).result("No schema URI given")
    } else {
      val wallet = VerifierConfig.config.wallets.get(walletId)!!
      ctx.status(HttpCode.FOUND).header("Location", "${wallet.url}/${wallet.presentPath}"+
          "?${VerifierManager.getService().newRequest(schemaUri).toUriQueryString()}")
    }
  }

  fun verifySIOPResponse(ctx: Context) {
    val state = ctx.formParam("state") ?: throw  BadRequestResponse("State not specified")
    val id_token = ctx.formParam("id_token") ?: throw BadRequestResponse("id_token not specified")
    val vp_token = ctx.formParam("vp_token") ?: throw BadRequestResponse("vp_token not specified")

    val result = VerifierManager.getService().verifyResponse(state, id_token, vp_token)

    ctx.status(HttpCode.FOUND).header("Location", VerifierManager.getService().getVerificationRedirectionUri(result).toString())
  }

  fun completeAuthentication(ctx: Context) {
    val access_token = ctx.queryParam("access_token")
    if(access_token == null) {
      ctx.status(HttpCode.FORBIDDEN)
      return
    }
    val result = VerifierManager.getService().getVerificationResult(access_token)
    if(result == null) {
      ctx.status(HttpCode.FORBIDDEN)
      return
    }
    ctx.json(result)
  }

  fun getProtectedData(ctx: Context) {
    val userInfo = JWTService.getUserInfo(ctx)
    if(userInfo != null) {
      ctx.result("Account balance: EUR 0.00")
    } else {
      ctx.status(HttpCode.FORBIDDEN)
    }
  }
}
