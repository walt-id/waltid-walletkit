package id.walt.issuer.backend

data class IssuableCredential (
  val id: String,
  val type: String,
  val description: String
)