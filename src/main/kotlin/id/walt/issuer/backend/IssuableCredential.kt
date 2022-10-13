package id.walt.issuer.backend

import id.walt.model.oidc.CredentialAuthorizationDetails
import id.walt.vclib.model.AbstractVerifiableCredential
import id.walt.vclib.model.CredentialSubject
import id.walt.vclib.templates.VcTemplateManager

data class IssuableCredential(
    val schemaId: String,
    val type: String,
    val credentialData: Map<String, Any>? = null
) {
    companion object {
        fun fromTemplateId(templateId: String): IssuableCredential {
            val tmpl = VcTemplateManager.loadTemplate(templateId)
            return IssuableCredential(
                tmpl.credentialSchema!!.id,
                tmpl.type.last(),
                mapOf(
                    Pair(
                        "credentialSubject",
                        (tmpl as AbstractVerifiableCredential<out CredentialSubject>).credentialSubject!!
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
    val credentialsBySchemaId
        get() = credentials.associateBy { it.schemaId }

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
