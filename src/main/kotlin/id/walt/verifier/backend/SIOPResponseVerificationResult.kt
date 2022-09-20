package id.walt.verifier.backend

import com.nimbusds.oauth2.sdk.AuthorizationRequest
import id.walt.auditor.VerificationResult
import id.walt.vclib.credentials.VerifiablePresentation

data class SIOPResponseVerificationResult(
  val state: String,
  val subject: String?,
  val request: AuthorizationRequest?,
  val verification_result: VerificationResult?,
  val vp_token: VerifiablePresentation?,
  var auth_token: String?
) {
  val isValid
    get() = !subject.isNullOrEmpty() && request != null && (verification_result?.valid ?: false)
}
