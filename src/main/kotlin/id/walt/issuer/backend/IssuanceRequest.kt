package id.walt.issuer.backend

data class IssuanceRequest (
  val user: String,
  val nonce: String,
  val selectedCredentialIds: Set<String>,
  val params: Map<String, List<String>>
)