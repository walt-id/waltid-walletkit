package id.walt.issuer.backend
import com.beust.klaxon.Klaxon
import com.nimbusds.oauth2.sdk.*
import com.nimbusds.oauth2.sdk.http.HTTPRequest
import com.nimbusds.oauth2.sdk.http.ServletUtils
import com.nimbusds.oauth2.sdk.id.Issuer
import com.nimbusds.openid.connect.sdk.Nonce
import com.nimbusds.openid.connect.sdk.SubjectType
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata
import id.walt.common.OidcUtil
import id.walt.model.dif.CredentialManifest
import id.walt.model.dif.OutputDescriptor
import id.walt.model.siopv2.*
import id.walt.services.jwt.JwtService
import id.walt.vclib.credentials.VerifiablePresentation
import id.walt.vclib.model.toCredential
import id.walt.vclib.templates.VcTemplateManager
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
import java.net.URI
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
        path("oidc") {
          get("meta", documented(
              document().operation {
                it.summary("get OIDC provider meta data")
                  .addTagsItem("issuer")
                  .operationId("oidcProviderMeta")
              }
                .json<OIDCProviderMetadata>("200"),
              IssuerController::oidcProviderMeta
            ))
          post("nonce", documented(
            document().operation {
              it.summary("get presentation nonce")
                .addTagsItem("issuer")
                .operationId("nonce")
            }
              .json<NonceResponse>("200"),
            IssuerController::nonce
          ))
          post("par", documented(
            document().operation {
              it.summary("pushed authorization request")
                .addTagsItem("issuer")
                .operationId("par")
            }
              .json<PushedAuthorizationSuccessResponse>("200"),
            IssuerController::par
          ))
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

  fun oidcProviderMeta(ctx: Context) {
    ctx.json(OIDCProviderMetadata(
      Issuer(IssuerConfig.config.issuerApiUrl),
      listOf(SubjectType.PAIRWISE, SubjectType.PUBLIC),
      URI("http://blank")).apply {
        authorizationEndpointURI = URI("${IssuerConfig.config.issuerUiUrl}/login")
        pushedAuthorizationRequestEndpointURI = URI("${IssuerConfig.config.issuerApiUrl}/oidc/par")
        tokenEndpointURI = URI("${IssuerConfig.config.issuerApiUrl}/oidc/token")
        setCustomParameter("credential_endpoint", "${IssuerConfig.config.issuerApiUrl}/oidc/credential")
        setCustomParameter("nonce_endpoint", "${IssuerConfig.config.issuerApiUrl}/oidc/nonce")
        setCustomParameter("credential_manifests", listOf(
          CredentialManifest(
            issuer = id.walt.model.dif.Issuer(IssuerManager.issuerDid, IssuerConfig.config.issuerClientName),
            outputDescriptors = listOf(
               OutputDescriptor("VerifiableID", VcTemplateManager.loadTemplate("VerifiableId").credentialSchema!!.id, "Verifiable ID document")
            )
          )).map { net.minidev.json.parser.JSONParser().parse(Klaxon().toJsonString(it)) }
        )
    }.toJSONObject())
  }

  fun nonce(ctx: Context) {
    ctx.json(IssuerManager.newNonce())
  }

  fun par(ctx: Context) {
    try {
      val response = IssuerManager.pushAuthorizationRequest(
        PushedAuthorizationRequest.parse(ServletUtils.createHTTPRequest(ctx.req))
      )
      if(response.indicatesSuccess())
        ctx.json(response.toSuccessResponse().toJSONObject())

      ctx.status(response.toErrorResponse().errorObject.httpStatusCode).json(response.toErrorResponse())
    } catch (exc: ParseException) {
      ctx.status(HttpCode.BAD_REQUEST).json(PushedAuthorizationErrorResponse(ErrorObject("400", "Error parsing PAR")))
    }
  }
}
