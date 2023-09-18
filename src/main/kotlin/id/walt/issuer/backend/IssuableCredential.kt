package id.walt.issuer.backend

import id.walt.credentials.w3c.JsonConverter
import id.walt.credentials.w3c.templates.VcTemplateManager
import id.walt.model.oidc.CredentialAuthorizationDetails
import kotlinx.serialization.json.JsonObject

data class IssuableCredential(
    val type: String,
    val credentialData: JsonObject? = null
) {
    companion object {
        fun fromTemplateId(templateId: String): IssuableCredential {
            val tmpl = VcTemplateManager.getTemplate(templateId, true).template!!
            return IssuableCredential(
                templateId,
                JsonObject(
                    mapOf(
                        "credentialSubject" to
                        tmpl.credentialSubject!!.toJsonObject()
                    )
                )
            )
        }
    }
}

data class Issuables(
    val credentials: List<IssuableCredential>
) {

    /* DEPRECATED: Does not work for issuing two credentials of the same type (but with different data)
    val credentialsByType
        get() = credentials.associateBy { it.type }
     */

    companion object {
        fun fromCredentialAuthorizationDetails(credentialDetails: List<CredentialAuthorizationDetails>): Issuables {
            return Issuables(
                credentials = credentialDetails.map { IssuableCredential.fromTemplateId(it.credential_type) }
            )
        }
    }
}

data class NonceResponse(
    val p_nonce: String,
    val expires_in: String? = null
)
