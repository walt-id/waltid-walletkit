package id.walt.webwallet.backend.wallet

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.google.common.cache.CacheBuilder
import com.nimbusds.oauth2.sdk.PushedAuthorizationResponse
import com.nimbusds.oauth2.sdk.http.HTTPRequest
import com.nimbusds.openid.connect.sdk.token.OIDCTokens
import id.walt.model.oidc.Claims
import id.walt.model.oidc.CredentialClaim
import id.walt.model.oidc.CredentialResponse
import id.walt.model.oidc.SIOPv2Request
import id.walt.webwallet.backend.config.WalletConfig
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
data class CredentialIssuanceRequest (
  val issuerId: String,
  val schemaIds: List<String>,
  val walletRedirectUri: String
)

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
data class CredentialIssuanceSession (
  val id: String,
  val issuanceRequest: CredentialIssuanceRequest,
  var tokens: OIDCTokens? = null,
  var tokenExpires: Instant? = null,
  var response: List<CredentialResponse>? = null
)

object CredentialIssuanceManager {
  val EXPIRATION_TIME = Duration.ofMinutes(5)
  val sessionCache = CacheBuilder.newBuilder().expireAfterAccess(EXPIRATION_TIME.seconds, TimeUnit.SECONDS).build<String, CredentialIssuanceSession>()

  fun initIssuance(issuanceRequest: CredentialIssuanceRequest): String? {
    val issuer = WalletConfig.config.issuers[issuanceRequest.issuerId]!!

    if(issuer.metadata == null) {
      return null
    }

    val session = CredentialIssuanceSession(
      id = UUID.randomUUID().toString(),
      issuanceRequest = issuanceRequest)

    val siopRequest = SIOPv2Request(
      response_type = "code",
      client_id = WalletConfig.config.walletApiUrl,
      redirect_uri = "${WalletConfig.config.walletApiUrl}/wallet/siopv2/finalizeIssuance",
      response_mode = "query",
      expiration = Instant.now().plusSeconds(EXPIRATION_TIME.seconds).epochSecond,
      issuedAt = Instant.now().epochSecond,
      claims = Claims(credentials = issuanceRequest.schemaIds.map {
        CredentialClaim(type = it, manifest_id = null)
      }),
      state = session.id,
      registration = null
    )

    val req = HTTPRequest(HTTPRequest.Method.POST, URI.create("${issuer.metadata!!.pushedAuthorizationRequestEndpointURI}"))
    req.query = siopRequest.toUriQueryString()
    val response = req.send()
    val parResponse = PushedAuthorizationResponse.parse(response)
    if(parResponse.indicatesSuccess()) {
      sessionCache.put(session.id, session)
      return "${issuer.metadata!!.authorizationEndpointURI}?client_id=${siopRequest.client_id}&request_uri=${parResponse.toSuccessResponse().requestURI}"
    } else {
      return null
    }
  }

  fun finalizeIssuance(id: String, code: String) {
    val issuance = sessionCache.getIfPresent(id)
    // TODO: get access token, get credentials, etc
  }
}
