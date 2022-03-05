package id.walt.webwallet.backend.wallet

import com.beust.klaxon.Klaxon
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.google.common.cache.CacheBuilder
import com.nimbusds.openid.connect.sdk.token.OIDCTokens
import id.walt.custodian.Custodian
import id.walt.model.dif.CredentialManifest
import id.walt.model.oidc.*
import id.walt.services.context.ContextManager
import id.walt.services.oidc.OIDC4CIService
import id.walt.services.oidc.OIDC4VPService
import id.walt.vclib.credentials.VerifiablePresentation
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.model.toCredential
import id.walt.webwallet.backend.auth.UserInfo
import id.walt.webwallet.backend.config.WalletConfig
import id.walt.webwallet.backend.context.WalletContextManager
import org.bouncycastle.asn1.crmf.SubsequentMessage
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
  @JsonIgnore val nonce: String,
  @JsonIgnore  val user: UserInfo,
  @JsonIgnore var tokens: OIDCTokens? = null,
  @JsonIgnore var lastTokenUpdate: Instant? = null,
  @JsonIgnore var tokenNonce: String? = null,
  var credentials: List<VerifiableCredential>? = null
)

object CredentialIssuanceManager {
  val EXPIRATION_TIME = Duration.ofMinutes(5)
  val sessionCache = CacheBuilder.newBuilder().expireAfterAccess(EXPIRATION_TIME.seconds, TimeUnit.SECONDS).build<String, CredentialIssuanceSession>()
  val redirectURI: URI
    get() = URI.create("${WalletConfig.config.walletApiUrl}/wallet/siopv2/finalizeIssuance")

  private fun generateRequiredVpTokenFor(schemaId: String, did: String, issuer: OIDCProvider): List<VerifiablePresentation>? {
    return issuer.ciSvc.credentialManifests.filter { m -> m.outputDescriptors.any { od -> od.schema == schemaId } }.map { manifest ->
      manifest.presentationDefinition?.let { presentationDefinition ->
        val nonceResponse = issuer.ciSvc.getNonce() ?: return null
        Custodian.getService().createPresentation(
          vcs = Custodian.getService().listCredentials().filter {
                cred -> cred.subject == did &&
                cred.credentialSchema?.id?.let { credSchemaId ->
                  presentationDefinition.input_descriptors.any { id -> id.schema.uri == credSchemaId }
                } ?: false
            }.map { cred -> cred.encode() },
          holderDid = did,
          challenge = nonceResponse.p_nonce,
          expirationDate = null)
      }?.toCredential()?.let { it as VerifiablePresentation }
    }.filterNotNull().ifEmpty {
      null // don't generate vp_token if no matching presentations are required
    }
  }

  fun initIssuance(issuanceRequest: CredentialIssuanceRequest, user: UserInfo): URI? {
    val issuer = WalletConfig.config.issuers[issuanceRequest.issuerId] ?: return null

    val session = CredentialIssuanceSession(
      id = UUID.randomUUID().toString(),
      issuanceRequest = issuanceRequest,
      nonce = UUID.randomUUID().toString(),
      user = user)

    val claimedCredentials = issuanceRequest.schemaIds.map {
      CredentialClaim(type = it, manifest_id = null, vp_token = generateRequiredVpTokenFor(it, issuanceRequest.did, issuer))
    }

    return issuer.ciSvc.executePushedAuthorizationRequest(redirectURI, claimedCredentials, nonce = session.nonce, state = session.id)?.also {
      putSession(session)
    }
  }

  private fun enc(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)

  fun finalizeIssuance(id: String, code: String): CredentialIssuanceSession? {
    val session = sessionCache.getIfPresent(id) ?: return null

    val issuer = WalletConfig.config.issuers[session.issuanceRequest.issuerId] ?: return null

    val tokenResponse = issuer.ciSvc.getAccessToken(code, redirectURI.toString())
    if(!tokenResponse.indicatesSuccess()) {
      return session
    }
    session.tokens = tokenResponse.toSuccessResponse().oidcTokens
    session.lastTokenUpdate = Instant.now()
    tokenResponse.customParameters["c_nonce"]?.let { it.toString() }?.also {
      session.tokenNonce = it
    }

    ContextManager.runWith(WalletContextManager.getUserContext(session.user)) {
      session.credentials = session.issuanceRequest.schemaIds.map { schemaId ->
        issuer.ciSvc.getCredential(session.tokens!!.accessToken, session.issuanceRequest.did, schemaId, issuer.ciSvc.generateDidProof(session.issuanceRequest.did, session.tokenNonce))
      }.filterNotNull().map { it!! }

      session.credentials?.forEach {
        it.id = it.id ?: UUID.randomUUID().toString()
        Custodian.getService().storeCredential(it.id!!, it)
      }
    }

    return session
  }

  fun getSession(id: String): CredentialIssuanceSession? {
    return sessionCache.getIfPresent(id)
  }

  fun putSession(session: CredentialIssuanceSession) {
    sessionCache.put(session.id, session)
  }

  fun findIssuersFor(requiredSchemaIds: Set<String>): List<OIDCProvider> {
    return WalletConfig.config.issuers.values.filter { issuer ->
      issuer.ciSvc.credentialManifests
          ?.flatMap { manifest -> manifest.outputDescriptors.map { outDesc -> outDesc.schema } }
          ?.toSet()
          ?.containsAll(requiredSchemaIds) ?: false
    }.map {
      OIDCProvider(it.id, it.url, it.description) // strip secrets
    }
  }
}
