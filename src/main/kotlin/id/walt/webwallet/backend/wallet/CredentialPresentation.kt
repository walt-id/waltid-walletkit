package id.walt.webwallet.backend.wallet

import com.beust.klaxon.Json
import com.google.common.cache.CacheBuilder
import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.nimbusds.oauth2.sdk.ResponseMode
import id.walt.common.KlaxonWithConverters
import id.walt.credentials.w3c.templates.VcTemplateManager
import id.walt.credentials.w3c.toVerifiablePresentation
import id.walt.custodian.Custodian
import id.walt.model.dif.InputDescriptor
import id.walt.model.dif.PresentationDefinition
import id.walt.model.oidc.OIDCProvider
import id.walt.model.oidc.SIOPv2Response
import id.walt.services.oidc.OIDC4VPService
import id.walt.services.oidc.OIDCUtils
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
    val state: String?,
    val fulfilled: Boolean,
    val rp_response: String?
) {
    companion object {
        fun fromSiopResponse(siopResp: SIOPv2Response, fulfilled: Boolean, rp_response: String?): PresentationResponse {
            return PresentationResponse(
                OIDCUtils.toVpToken(siopResp.vp_token),
                KlaxonWithConverters().toJsonString(siopResp.presentation_submission),
                siopResp.id_token,
                siopResp.state, fulfilled, rp_response
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
        return OIDCUtils.findCredentialsFor(session.sessionInfo.presentationDefinition, session.sessionInfo.did)
            .flatMap { kv ->
                kv.value.map { credId -> PresentableCredential(credId, kv.key) }
            }.toList()
    }

    private fun getRequiredSchemaIds(input_descriptors: List<InputDescriptor>): Set<String> {
        return VcTemplateManager.listTemplates().map { tmpl -> VcTemplateManager.getTemplate(tmpl.name, true).template!! }
            .filter { templ -> input_descriptors.any { indesc -> OIDCUtils.matchesInputDescriptor(templ, indesc) } }
            .map { templ -> templ.credentialSchema?.id }
            .filterNotNull()
            .toSet()
    }

    fun continueCredentialPresentationFor(sessionId: String, did: String): CredentialPresentationSession {
        val session =
            sessionCache.getIfPresent(sessionId) ?: throw IllegalArgumentException("No session found for id $sessionId")
        session.sessionInfo.did = did
        session.sessionInfo.presentableCredentials = getPresentableCredentials(session)
        session.sessionInfo.availableIssuers = null
        if (session.sessionInfo.presentableCredentials!!.isEmpty()) {
            if (session.sessionInfo.presentationDefinition.input_descriptors.isNotEmpty()) {
                // credentials are required, but no suitable ones are found
                session.sessionInfo.availableIssuers =
                    CredentialIssuanceManager.findIssuersFor(session.sessionInfo.presentationDefinition)
            }
        }

        return session
    }

    fun fulfillPresentation(sessionId: String, selectedCredentials: List<PresentableCredential>): PresentationResponse {
        val session =
            sessionCache.getIfPresent(sessionId) ?: throw IllegalArgumentException("No session found for id $sessionId")
        val did = session.sessionInfo.did ?: throw IllegalArgumentException("Did not set for this session")

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
        ).toVerifiablePresentation()

        val siopResponse = OIDC4VPService.getSIOPResponseFor(session.req, did, listOf(vp))
        val rp_response = if (ResponseMode("post") == session.req.responseMode) {
            OIDC4VPService.postSIOPResponse(session.req, siopResponse)
        } else null

        return PresentationResponse.fromSiopResponse(siopResponse, rp_response != null, rp_response)
    }

    fun getPresentationSession(id: String): CredentialPresentationSession? {
        return sessionCache.getIfPresent(id)
    }
}
