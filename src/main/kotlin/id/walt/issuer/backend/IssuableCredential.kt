package id.walt.issuer.backend

import id.walt.credentials.w3c.JsonConverter
import id.walt.credentials.w3c.templates.VcTemplateManager
import id.walt.model.oidc.CredentialAuthorizationDetails

data class IssuableCredential(
    val type: String,
    val credentialData: Map<String, Any>? = null
) {
    companion object {
        fun fromTemplateId(templateId: String): IssuableCredential {
            val tmpl = VcTemplateManager.getTemplate(templateId, true).template!!
            return IssuableCredential(
                tmpl.type.last(),
                mapOf(
                    Pair(
                        "credentialSubject",
                        JsonConverter.fromJsonElement(tmpl.credentialSubject!!.toJsonObject()) as Map<*, *>
                    )
                )
            )
        }
    }
}

data class Issuables(
    val credentials: List<IssuableCredential>
) {
    val credentialsByType
        get() = credentials.associateBy { it.type }

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
