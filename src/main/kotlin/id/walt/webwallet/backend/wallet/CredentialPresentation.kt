package id.walt.webwallet.backend.wallet

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import com.google.common.cache.CacheBuilder
import com.nimbusds.oauth2.sdk.AuthorizationRequest
import id.walt.custodian.Custodian
import id.walt.model.dif.InputDescriptor
import id.walt.model.dif.PresentationDefinition
import id.walt.model.oidc.OIDCProvider
import id.walt.model.oidc.SIOPv2Response
import id.walt.model.oidc.klaxon
import id.walt.services.oidc.OIDC4VPService
import id.walt.services.oidc.OIDCUtils
import id.walt.vclib.credentials.VerifiablePresentation
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.model.toCredential
import id.walt.vclib.templates.VcTemplateManager
import id.walt.webwallet.backend.auth.UserInfo
import java.time.Duration
import java.util.*
import java.util.concurrent.*

data class CredentialPresentationSessionInfo(
    val id: String,
    val presentationDefinition: PresentationDefinition,
    val redirectUri: String,
    var did: String? = null,
    var presentableCredentials: List<PresentableCredential>? = null,
    var availableIssuers: List<OIDCProvider>? = null
)

data class CredentialPresentationSession(
    val id: String,
    @Json(ignored = true) val req: AuthorizationRequest,
    val sessionInfo: CredentialPresentationSessionInfo
)

data class PresentableCredential(
    val credentialId: String,
    val claimId: String?
)

data class PresentationResponse(
    val vp_token: String,
    val presentation_submission: String,
    val id_token: String?,
    val state: String?
) {
    companion object {
        fun fromSiopResponse(siopResp: SIOPv2Response): PresentationResponse {
            return PresentationResponse(
                OIDCUtils.toVpToken(siopResp.vp_token),
                klaxon.toJsonString(siopResp.presentation_submission),
                siopResp.id_token,
                siopResp.state
            )
        }
    }
}

object CredentialPresentationManager {
    val EXPIRATION_TIME = Duration.ofMinutes(5)
    val sessionCache = CacheBuilder.newBuilder().expireAfterAccess(EXPIRATION_TIME.seconds, TimeUnit.SECONDS)
        .build<String, CredentialPresentationSession>()

    fun initCredentialPresentation(siopReq: AuthorizationRequest): CredentialPresentationSession {
        val id = UUID.randomUUID().toString()
        return CredentialPresentationSession(
            id = id,
            req = siopReq,
            sessionInfo = CredentialPresentationSessionInfo(
                id,
                presentationDefinition = OIDC4VPService.getPresentationDefinition(siopReq),
                redirectUri = siopReq.redirectionURI.toString()
            )
        ).also {
            sessionCache.put(it.id, it)
        }
    }

    private fun getPresentableCredentials(session: CredentialPresentationSession): List<PresentableCredential> {
        return OIDCUtils.findCredentialsFor(session.sessionInfo.presentationDefinition, session.sessionInfo.did).flatMap { kv ->
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
        session.sessionInfo.did = did
        session.sessionInfo.presentableCredentials = getPresentableCredentials(session)
        session.sessionInfo.availableIssuers = null
        if (session.sessionInfo.presentableCredentials!!.isEmpty()) {
            if (session.sessionInfo.presentationDefinition.input_descriptors.isNotEmpty()) {
                // credentials are required, but no suitable ones are found
                session.sessionInfo.availableIssuers = CredentialIssuanceManager.findIssuersFor(session.sessionInfo.presentationDefinition)
            }
        }

        return session
    }

    fun fulfillPresentation(sessionId: String, selectedCredentials: List<PresentableCredential>): SIOPv2Response {
        val session = sessionCache.getIfPresent(sessionId) ?: throw Exception("No session found for id $sessionId")
        val did = session.sessionInfo.did ?: throw Exception("Did not set for this session")

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

        val siopResponse = OIDC4VPService.getSIOPResponseFor(session.req, did, listOf(vp))

        return siopResponse
    }

    fun getPresentationSession(id: String): CredentialPresentationSession? {
        return sessionCache.getIfPresent(id)
    }
}
