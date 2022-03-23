package id.walt.verifier.backend

import id.walt.auditor.VerificationResult
import id.walt.model.oidc.SIOPv2Request
import id.walt.vclib.credentials.VerifiablePresentation

data class SIOPResponseVerificationResult(
  val state: String,
  val subject: String?,
  val request: SIOPv2Request?,
  val id_token_valid: Boolean,
  val verification_result: VerificationResult?,
  val vp_token: VerifiablePresentation?,
  var auth_token: String?
) {
  val isValid
    get() = !subject.isNullOrEmpty() && request != null && id_token_valid && (verification_result?.valid ?: false)
}
