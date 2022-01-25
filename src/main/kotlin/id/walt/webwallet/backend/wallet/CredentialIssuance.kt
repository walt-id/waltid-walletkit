package id.walt.webwallet.backend.wallet

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.google.common.cache.CacheBuilder
import com.nimbusds.oauth2.sdk.PushedAuthorizationResponse
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic
import com.nimbusds.oauth2.sdk.auth.Secret
import com.nimbusds.oauth2.sdk.http.HTTPRequest
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse
import com.nimbusds.openid.connect.sdk.token.OIDCTokens
import id.walt.custodian.Custodian
import id.walt.issuer.backend.IssuanceSession
import id.walt.model.oidc.*
import id.walt.services.context.ContextManager
import id.walt.vclib.model.toCredential
import id.walt.webwallet.backend.auth.UserInfo
import id.walt.webwallet.backend.config.WalletConfig
import id.walt.webwallet.backend.context.WalletContextManager
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
data class CredentialIssuanceRequest (
  val did: String,
  val issuerId: String,
  val schemaIds: List<String>,
  val walletRedirectUri: String
)

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
data class CredentialIssuanceSession (
  val id: String,
  val issuanceRequest: CredentialIssuanceRequest,
  val user: UserInfo,
  var tokens: OIDCTokens? = null,
  var tokenExpires: Instant? = null,
  var response: List<CredentialResponse>? = null
)

object CredentialIssuanceManager {
  val EXPIRATION_TIME = Duration.ofMinutes(5)
  val sessionCache = CacheBuilder.newBuilder().expireAfterAccess(EXPIRATION_TIME.seconds, TimeUnit.SECONDS).build<String, CredentialIssuanceSession>()

  fun initIssuance(issuanceRequest: CredentialIssuanceRequest, user: UserInfo): String? {
    val issuer = WalletConfig.config.issuers[issuanceRequest.issuerId]!!

    if(issuer.metadata == null) {
      return null
    }

    val session = CredentialIssuanceSession(
      id = UUID.randomUUID().toString(),
      issuanceRequest = issuanceRequest,
      user = user)

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

    val req = HTTPRequest(HTTPRequest.Method.POST, issuer.metadata!!.pushedAuthorizationRequestEndpointURI)
    if(!issuer.client_id.isNullOrEmpty() && !issuer.client_secret.isNullOrEmpty())
      req.authorization = ClientSecretBasic(ClientID(issuer.client_id), Secret(issuer.client_secret)).toHTTPAuthorizationHeader()
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

  private fun enc(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)

  fun finalizeIssuance(id: String, code: String): CredentialIssuanceSession? {
    val issuance = sessionCache.getIfPresent(id)
    val issuer = issuance?.let { WalletConfig.config.issuers[it.issuanceRequest.issuerId] }
    if(issuer?.metadata == null) {
      return issuance
    }
    // TODO: get access token, get credentials, etc
    val req = HTTPRequest(HTTPRequest.Method.POST, issuer.metadata!!.tokenEndpointURI)
    if(!issuer.client_id.isNullOrEmpty() && !issuer.client_secret.isNullOrEmpty())
      req.authorization = ClientSecretBasic(ClientID(issuer.client_id), Secret(issuer.client_secret)).toHTTPAuthorizationHeader()
    req.query = "code=${enc(code)}" +
                "&grant_type=authorization_code"
    val resp = req.send()
    val tokenResponse = OIDCTokenResponse.parse(resp)
    if(!tokenResponse.indicatesSuccess()) {
      return issuance
    }
    issuance.tokens = tokenResponse.toSuccessResponse().oidcTokens
    tokenResponse.customParameters["expires_in"]?.let { it.toString().toLong() }?.also {
      issuance.tokenExpires = Instant.now().plusSeconds(it)
    }

    issuance.response = issuance.issuanceRequest.schemaIds.map { schemaId ->
      val credReq = HTTPRequest(
        HTTPRequest.Method.POST,
        URI.create(issuer.metadata!!.customParameters["credential_endpoint"].toString())
      )
      credReq.authorization = issuance.tokens!!.accessToken.toAuthorizationHeader()
      credReq.query = "did=${issuance.issuanceRequest.did}&type=$schemaId"
      val resp = credReq.send()
      if(resp.indicatesSuccess())
        klaxon.parse<CredentialResponse>(resp.content)
      else
        return null
    }.filterNotNull().map { it!! }

    ContextManager.runWith(WalletContextManager.getUserContext(issuance.user)) {
      issuance.response!!.map { String(Base64.getUrlDecoder().decode(it.credential)).toCredential() }.forEach {
        it.id = it.id ?: UUID.randomUUID().toString()
        Custodian.getService().storeCredential(it.id!!, it)
      }
    }

    return issuance
  }

  fun getSession(id: String): CredentialIssuanceSession? {
    return sessionCache.getIfPresent(id)
  }
}
