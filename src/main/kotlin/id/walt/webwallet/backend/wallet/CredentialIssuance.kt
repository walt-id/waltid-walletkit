package id.walt.webwallet.backend.wallet

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.google.common.cache.CacheBuilder
import com.nimbusds.oauth2.sdk.PushedAuthorizationResponse
import com.nimbusds.oauth2.sdk.PushedAuthorizationSuccessResponse
import com.nimbusds.oauth2.sdk.http.HTTPRequest
import com.nimbusds.oauth2.sdk.http.HTTPResponse
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse
import com.nimbusds.openid.connect.sdk.token.OIDCTokens
import id.walt.model.oidc.Claims
import id.walt.model.oidc.CredentialClaim
import id.walt.model.oidc.CredentialResponse
import id.walt.model.oidc.SIOPv2Request
import id.walt.webwallet.backend.config.WalletConfig
import net.minidev.json.JSONObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
data class CredentialIssuance (
  val issuerId: String,
  val schemaId: String,
  val walletRedirectUri: String,
  var id: String? = null,
  var tokens: OIDCTokens? = null,
  var tokenExpires: Instant? = null,
  var response: CredentialResponse? = null
)

object CredentialIssuanceManager {
  val EXPIRATION_TIME = Duration.ofMinutes(5)
  val issuanceCache = CacheBuilder.newBuilder().expireAfterAccess(EXPIRATION_TIME.seconds, TimeUnit.SECONDS).build<String, CredentialIssuance>()

  fun initIssuance(issuance: CredentialIssuance): String? {
    val issuer = WalletConfig.config.issuers[issuance.issuerId]!!

    if(issuer.metadata == null) {
      return null
    }

    if(issuance.id == null)
      issuance.id = UUID.randomUUID().toString()

    val siopRequest = SIOPv2Request(
      response_type = "code",
      client_id = WalletConfig.config.walletApiUrl,
      redirect_uri = "${WalletConfig.config.walletApiUrl}/wallet/siopv2/finalizeIssuance",
      response_mode = "query",
      expiration = Instant.now().plusSeconds(EXPIRATION_TIME.seconds).epochSecond,
      issuedAt = Instant.now().epochSecond,
      claims = Claims(credentials = listOf(
        CredentialClaim(type = issuance.schemaId, manifest_id = null)
      )),
      state = issuance.id,
      registration = null
    )

    val req = HTTPRequest(HTTPRequest.Method.POST, URI.create("${issuer.metadata!!.pushedAuthorizationRequestEndpointURI}"))
    req.query = siopRequest.toUriQueryString()
    val response = req.send()
    val parResponse = PushedAuthorizationResponse.parse(response)
    if(parResponse.indicatesSuccess()) {
      issuanceCache.put(issuance.id, issuance)
      return "${issuer.metadata!!.authorizationEndpointURI}?client_id=${siopRequest.client_id}&request_uri=${parResponse.toSuccessResponse().requestURI}"
    } else {
      return null
    }
  }

  fun finalizeIssuance(id: String, code: String) {
    val issuance = issuanceCache.getIfPresent(id)
    // TODO: get access token, get credentials, etc
  }
}
