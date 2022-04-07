package id.walt.verifier.backend
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.auth.UserRole
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.HttpCode
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
              .queryParam<String>("schemaUri", isRepeatable = true)
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
    val wallet = ctx.queryParam("walletId")?.let { VerifierConfig.config.wallets.get(it) } ?: throw BadRequestResponse("Unknown or missing walletId")
    val schemaUris = ctx.queryParams("schemaUri")
    if(schemaUris.isEmpty()) {
      throw BadRequestResponse("No schema URI(s) given")
    }
    val customQueryParams = ctx.queryParamMap().keys.filter { k -> k != "walletId" && k != "schemaUri" }.flatMap { k ->
      ctx.queryParams(k).map { v -> "$k=${URLEncoder.encode(v, StandardCharsets.UTF_8)}" }
    }.joinToString("&" )
    ctx.status(HttpCode.FOUND).header("Location", "${wallet.url}/${wallet.presentPath}"+
          "?${VerifierManager.getService().newRequest(schemaUris.toSet(), redirectCustomUrlQuery = customQueryParams).toUriQueryString()}")
  }

  fun verifySIOPResponse(ctx: Context) {
    val state = ctx.formParam("state") ?: throw  BadRequestResponse("State not specified")
    val id_token = ctx.formParam("id_token") ?: throw BadRequestResponse("id_token not specified")
    val vp_token = ctx.formParam("vp_token") ?: throw BadRequestResponse("vp_token not specified")
    val verifierUiUrl = ctx.queryParam("verifierUiUrl") ?: VerifierConfig.config.verifierUiUrl
    val result = VerifierManager.getService().verifyResponse(state, id_token, vp_token)

    ctx.status(HttpCode.FOUND).header("Location", VerifierManager.getService().getVerificationRedirectionUri(result, verifierUiUrl).toString())
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
