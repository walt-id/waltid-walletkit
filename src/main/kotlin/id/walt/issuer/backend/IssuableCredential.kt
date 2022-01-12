package id.walt.issuer.backend

import id.walt.vclib.model.VerifiableCredential

data class IssuableCredential (
  val type: String,
  val description: String,
  val credentialData: Map<String, Any>? = null
)

data class Issuables (
  val credentials: Map<String, IssuableCredential>
    )

data class NonceResponse(
  val p_nonce: String,
  val expires_in: String? = null
)
