package id.walt.issuer.backend

import id.walt.model.oidc.CredentialClaim
import id.walt.vclib.model.AbstractVerifiableCredential
import id.walt.vclib.model.CredentialSubject
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.registry.VcTypeRegistry
import id.walt.vclib.templates.VcTemplateManager
import io.javalin.core.util.RouteOverviewUtil.metaInfo

data class IssuableCredential (
  val schemaId: String,
  val type: String,
  val credentialData: Map<String, Any>? = null
) {
  companion object {
    fun fromTemplateId(templateId: String): IssuableCredential {
      val tmpl = VcTemplateManager.loadTemplate(templateId)
      return IssuableCredential(
        tmpl!!.credentialSchema!!.id,
        tmpl.type.last(),
        mapOf(Pair("credentialSubject", (tmpl as AbstractVerifiableCredential<out CredentialSubject>).credentialSubject!!)))
    }
  }
}

data class Issuables (
  val credentials: Map<String, IssuableCredential>
    )
{
  companion object {
    fun fromCredentialClaims(credentialClaims: List<CredentialClaim>): Issuables {
      return Issuables(
        credentials = credentialClaims.flatMap { claim -> VcTypeRegistry.getTypesWithTemplate().values
          .map { it.metadata.template!!() }
          .filter { it.credentialSchema != null }
          .filter { it.credentialSchema!!.id == claim.type }
          .map { it.type.last() }
        } .map { IssuableCredential.fromTemplateId(it) }
          .associateBy { it.type }
      )
    }
  }
}

data class NonceResponse(
  val p_nonce: String,
  val expires_in: String? = null
)
