package id.walt.verifier.backend

import id.walt.auditor.VerificationResult
import id.walt.model.siopv2.SIOPv2Request

data class ResponseVerification(
  val id: String,
  val subject: String?,
  val request: SIOPv2Request?,
  val id_token: Boolean,
  val vp_token: VerificationResult?,
  var auth_token: String?
) {
  val isValid
    get() = !subject.isNullOrEmpty() && request != null && id_token && (vp_token?.valid ?: false)
}
