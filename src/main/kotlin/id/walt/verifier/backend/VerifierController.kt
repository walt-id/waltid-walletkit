package id.walt.verifier.backend
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.auth.UserRole
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.Context
import io.javalin.http.HttpCode
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented
import org.apache.http.HttpStatus

object VerifierController {
  val routes
    get() =
      path("") {
        path("wallets") {
          get("list", documented(
            document().operation {
              it.summary("List wallet configurations")
                .addTagsItem("verifier")
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
                .addTagsItem("verifier")
                .operationId("presentVID")
            }
              .queryParam<String>("walletId")
              .queryParam<String>("schemaUri")
              .result<String>("302"),
            VerifierController::presentCredential
          ))
        }
        path("verify") {
          post("{nonce}", documented(
            document().operation {
              it.summary("SIOPv2 request verification callback")
                .addTagsItem("verifier")
                .operationId("verifySIOPv2Request")
            }
              .formParamBody<String> { }
              .result<String>("302"),
            VerifierController::verifySIOPv2Request
          ))
        }
        path("auth") {
          get(documented(
            document().operation {
              it.summary("Complete authentication by siopv2 verification")
                .addTagsItem("verifier")
                .operationId("completeAuthentication")
            }
              .queryParam<String>("access_token")
              .json<ResponseVerification>("200"),
            VerifierController::completeAuthentication
          ))
        }
        path("protected") {
          get(documented(
            document().operation {
              it.summary("Fetch protected data (example)")
                .addTagsItem("verifier")
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
          "?${SIOPv2RequestManager.newRequest(schemaUri).toUriQueryString()}")
    }
  }

  fun verifySIOPv2Request(ctx: Context) {
    // TODO: verify siop response
    val nonce = ctx.pathParam("nonce")
    val id_token = ctx.formParam("id_token")
    val vp_token = ctx.formParam("vp_token")

    if(nonce.isNullOrEmpty() || id_token.isNullOrEmpty() || vp_token.isNullOrEmpty()) {
      ctx.status(HttpStatus.SC_BAD_REQUEST).result("Missing required parameters")
      return
    }

    val result = SIOPv2RequestManager.verifyResponse(nonce, id_token, vp_token)

    ctx.status(HttpCode.FOUND).header("Location", "${VerifierConfig.config.verifierUiUrl}/success/?access_token=${result?.id ?: ""}")
  }

  fun completeAuthentication(ctx: Context) {
    val access_token = ctx.queryParam("access_token")
    if(access_token == null) {
      ctx.status(HttpCode.FORBIDDEN)
      return
    }
    val result = SIOPv2RequestManager.getVerificationResult(access_token)
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
