package id.walt.issuer.backend
import com.beust.klaxon.Klaxon
import id.walt.common.OidcUtil
import id.walt.model.siopv2.*
import id.walt.services.jwt.JwtService
import id.walt.vclib.credentials.VerifiablePresentation
import id.walt.vclib.model.toCredential
import id.walt.verifier.backend.SIOPv2RequestManager
import id.walt.verifier.backend.VerifierConfig
import id.walt.verifier.backend.VerifierController
import id.walt.verifier.backend.WalletConfiguration
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.auth.UserRole
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.Context
import io.javalin.http.HttpCode
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented
import java.time.Instant
import java.util.*

object IssuerController {
  val routes
    get() =
      path("") {
        path("wallets") {
          get("list", documented(
            document().operation {
              it.summary("List wallet configurations")
                .addTagsItem("issuer")
                .operationId("listWallets")
            }
              .jsonArray<WalletConfiguration>("200"),
            VerifierController::listWallets,
          ))
        }
        path("credentials") {
          get("listIssuables", documented(
            document().operation {
              it.summary("List issuable credentials")
                .addTagsItem("issuer")
                .operationId("listIssuableCredentials")
            }
              .json<Issuables>("200"),
            IssuerController::listIssuableCredentials), UserRole.AUTHORIZED)
          path("issuance") {
            post("request", documented(
              document().operation {
                it.summary("Request issuance of selected credentials to wallet")
                  .addTagsItem("issuer")
                  .operationId("requestIssuance")
              }
                .queryParam<String>("walletId")
                .body<Issuables>()
                .result<String>("200"),
              IssuerController::requestIssuance
            ), UserRole.AUTHORIZED)
            post("fulfill/{nonce}", documented(
              document().operation {
                it.summary("SIOPv2 issuance fulfillment callback")
                  .addTagsItem("issuer")
                  .operationId("fulfillIssuance")
              }
                .formParamBody<String> { }
                .jsonArray<String>("200"),
              IssuerController::fulfillIssuance
            ))
          }
        }
      }

  fun listIssuableCredentials(ctx: Context) {
    val userInfo = JWTService.getUserInfo(ctx)
    if(userInfo == null) {
      ctx.status(HttpCode.UNAUTHORIZED)
      return
    }

    ctx.json(IssuerManager.listIssuableCredentialsFor(userInfo!!.email))
  }

  fun requestIssuance(ctx: Context) {
    val userInfo = JWTService.getUserInfo(ctx)
    if(userInfo == null) {
      ctx.status(HttpCode.UNAUTHORIZED)
      return
    }

    val walletId = ctx.queryParam("walletId")
    if (walletId.isNullOrEmpty() || !VerifierConfig.config.wallets.contains(walletId)) {
      ctx.status(HttpCode.BAD_REQUEST).result("Unknown wallet ID given")
      return
    }

    val selectedIssuables = ctx.bodyAsClass<Issuables>()
    if(selectedIssuables.credentials.isEmpty()) {
      ctx.status(HttpCode.BAD_REQUEST).result("No issuable credential selected")
      return;
    }

    val wallet = VerifierConfig.config.wallets.get(walletId)!!
    ctx.result(
      "${wallet.url}/${wallet.receivePath}" +
          "?${
            IssuerManager.newIssuanceRequest(userInfo.email, selectedIssuables).toUriQueryString()
          }")
  }

  fun fulfillIssuance(ctx: Context) {
    val id_token = ctx.formParam("id_token")
    val vp_token = ctx.formParam("vp_token")?.toCredential() as VerifiablePresentation
    //TODO: verify and parse id token
    val nonce = ctx.pathParam("nonce")
    ctx.result(
      "[ ${IssuerManager.fulfillIssuanceRequest(nonce, null, vp_token).joinToString(",") } ]"
    )
  }
}
