package id.walt.issuer.backend

import id.walt.model.oidc.CredentialClaim
import id.walt.vclib.model.AbstractVerifiableCredential
import id.walt.vclib.model.CredentialSubject
import id.walt.vclib.registry.VcTypeRegistry
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
        fun fromCredentialClaims(credentialClaims: List<CredentialClaim>): Issuables {
            return Issuables(
                credentials = credentialClaims.flatMap { claim ->
                    VcTypeRegistry.getTypesWithTemplate().values
                        .map { it.metadata.template!!() }
                        .filter { it.credentialSchema != null }
                        .filter {
                            (isSchema(claim.type!!) && it.credentialSchema!!.id == claim.type) ||
                                    (!isSchema(claim.type!!) && it.type.last() == claim.type)
                        }
                        .map { it.type.last() }
                }.map { IssuableCredential.fromTemplateId(it) }
            )
        }
    }
}

data class NonceResponse(
    val p_nonce: String,
    val expires_in: String? = null
)
