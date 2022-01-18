package id.walt.issuer.backend

import id.walt.model.oidc.CredentialClaim
import id.walt.vclib.model.AbstractVerifiableCredential
import id.walt.vclib.model.CredentialSubject
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.registry.VcTypeRegistry
import io.javalin.core.util.RouteOverviewUtil.metaInfo

data class IssuableCredential (
  val type: String,
  val description: String,
  val credentialData: Map<String, Any>? = null
)

data class Issuables (
  val credentials: Map<String, IssuableCredential>
    )
{
  companion object {
    fun fromCredentialClaims(credentialClaims: List<CredentialClaim>): Issuables {
      val pairs = credentialClaims.map { claim ->
        val credTmp = VcTypeRegistry.getTypesWithTemplate().values.firstOrNull { claim.type == it.metadata.template?.invoke()?.credentialSchema?.id }
        val credName = credTmp?.metadata?.type?.lastOrNull() ?: claim.type!!
        Pair(credName, IssuableCredential(credName, "", mapOf(
          Pair("credentialSubject", (credTmp?.metadata?.template?.invoke() as AbstractVerifiableCredential<out CredentialSubject>).credentialSubject!!)
        )))
      }
      return Issuables(
        credentials = pairs.toMap()
      )
    }
  }
}

data class NonceResponse(
  val p_nonce: String,
  val expires_in: String? = null
)
