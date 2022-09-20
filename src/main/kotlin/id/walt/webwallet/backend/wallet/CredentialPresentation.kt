package id.walt.webwallet.backend.wallet

import com.beust.klaxon.Klaxon
import com.google.common.cache.CacheBuilder
import com.nimbusds.oauth2.sdk.AuthorizationRequest
import id.walt.custodian.Custodian
import id.walt.model.dif.InputDescriptor
import id.walt.model.dif.PresentationDefinition
import id.walt.model.oidc.OIDCProvider
import id.walt.model.oidc.SIOPv2Response
import id.walt.services.oidc.OIDC4VPService
import id.walt.services.oidc.OIDCUtils
import id.walt.vclib.credentials.VerifiablePresentation
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.model.toCredential
import id.walt.vclib.templates.VcTemplateManager
import id.walt.webwallet.backend.auth.UserInfo
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

data class CredentialPresentationSession (
  val id: String,
  val req: AuthorizationRequest,
  val presentationDefinition: PresentationDefinition,
  val isPassiveIssuanceSession: Boolean,
  var did: String? = null,
  var presentableCredentials: List<PresentableCredential>? = null,
  var availableIssuers: List<OIDCProvider>? = null
  ) {
  val redirectUri: String
    get() = req.redirectionURI.toString()
}

data class PresentableCredential(
  val credentialId: String,
  val claimId: String?
)

data class PresentationResponse(
  val id_token: String?,
  val vp_token: String?,
  val state: String?
) {
  companion object {
    fun fromSiopResponse(siopResp: SIOPv2Response): PresentationResponse {
      return PresentationResponse(
        siopResp.id_token,
        OIDCUtils.toVpToken(siopResp.vp_token),
        siopResp.state
      )
    }
  }
}

object CredentialPresentationManager {
  val EXPIRATION_TIME = Duration.ofMinutes(5)
  val sessionCache = CacheBuilder.newBuilder().expireAfterAccess(EXPIRATION_TIME.seconds, TimeUnit.SECONDS).build<String, CredentialPresentationSession>()

  fun initCredentialPresentation(siopReq: AuthorizationRequest, passiveIssuance: Boolean): CredentialPresentationSession {
    return CredentialPresentationSession(
      id = UUID.randomUUID().toString(),
      req = siopReq,
      presentationDefinition = OIDC4VPService.getPresentationDefinition(siopReq),
      isPassiveIssuanceSession = passiveIssuance
    ).also {
      sessionCache.put(it.id, it)
    }
  }

  private fun getPresentableCredentials(session: CredentialPresentationSession): List<PresentableCredential> {
    return OIDCUtils.findCredentialsFor(session.presentationDefinition, session.did).flatMap { kv ->
        kv.value.map { credId -> PresentableCredential(credId, kv.key) }
      }.toList()
  }

  private fun getRequiredSchemaIds(input_descriptors: List<InputDescriptor>): Set<String> {
    return VcTemplateManager.getTemplateList().map { tmplId -> VcTemplateManager.loadTemplate(tmplId) }
      .filter { templ -> input_descriptors.any { indesc -> OIDCUtils.matchesInputDescriptor(templ, indesc) } }
      .map { templ -> templ.credentialSchema?.id }
      .filterNotNull()
      .toSet()
  }

  fun continueCredentialPresentationFor(sessionId: String, did: String): CredentialPresentationSession {
    val session = sessionCache.getIfPresent(sessionId) ?: throw Exception("No session found for id $sessionId")
    session.did = did
    session.presentableCredentials = getPresentableCredentials(session)
    session.availableIssuers = null
    if(session.presentableCredentials!!.isEmpty()) {
      val requiredSchemaIds = session.presentationDefinition?.input_descriptors?.let { getRequiredSchemaIds(it) } ?: setOf()
      if(requiredSchemaIds.isNotEmpty()) {
        // credentials are required, but no suitable ones are found
        session.availableIssuers = CredentialIssuanceManager.findIssuersFor(requiredSchemaIds)
      }
    }

    return session
  }

  fun fulfillPresentation(sessionId: String, selectedCredentials: List<PresentableCredential>): SIOPv2Response {
    val session = sessionCache.getIfPresent(sessionId) ?: throw Exception("No session found for id $sessionId")
    val did = session.did ?: throw  Exception("Did not set for this session")

    val myCredentials = Custodian.getService().listCredentials()
    val selectedCredentialIds = selectedCredentials.map { cred -> cred.credentialId }.toSet()
    val selectedCredentials =
      myCredentials.filter { cred -> selectedCredentialIds.contains(cred.id) }.map { cred -> cred.encode() }
        .toList()
    val vp = Custodian.getService().createPresentation(
      selectedCredentials,
      did,
      null,
      challenge = session.req.getCustomParameter("nonce")?.firstOrNull(),
      expirationDate = null
    ).toCredential() as VerifiablePresentation

    val vpSvc = OIDC4VPService(OIDCProvider("", ""))
    val siopResponse = vpSvc.getSIOPResponseFor(session.req, did, listOf(vp))

    return siopResponse
  }

  fun fulfillPassiveIssuance(sessionId: String, selectedCredentials: List<PresentableCredential>, userInfo: UserInfo): CredentialIssuanceSession {
    val session = sessionCache.getIfPresent(sessionId) ?: throw Exception("No session found for id $sessionId")
    val siopResponse = fulfillPresentation(sessionId, selectedCredentials)

    val vpSvc = OIDC4VPService(OIDCProvider("", ""))
    val body = vpSvc.postSIOPResponse(session.req, siopResponse)
    val credentials = Klaxon().parseArray<VerifiableCredential>(body)?.also { creds ->
      creds.forEach { cred ->
        Custodian.getService().storeCredential(cred.id!!, cred)
      }
    }

    return CredentialIssuanceSession(
      UUID.randomUUID().toString(),
      issuanceRequest = CredentialIssuanceRequest(did = session.did!!, issuerId = "", schemaIds = listOf(), walletRedirectUri = ""),
      nonce = session.req.getCustomParameter("nonce")?.firstOrNull() ?: "",
      user = userInfo,
      credentials = credentials
    ).also {
      CredentialIssuanceManager.putSession(it)
    }
  }
}
