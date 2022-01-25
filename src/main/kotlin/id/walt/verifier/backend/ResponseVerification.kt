package id.walt.verifier.backend

import id.walt.auditor.VerificationResult
import id.walt.model.oidc.SIOPv2Request

data class ResponseVerification(
  val id: String,
  val subject: String?,
  val request: SIOPv2Request?,
  val id_token: Boolean,
  val verificationResult: VerificationResult?,
  val vp_token: String?,
  var auth_token: String?
) {
  val isValid
    get() = !subject.isNullOrEmpty() && request != null && id_token && (verificationResult?.valid ?: false)
}
