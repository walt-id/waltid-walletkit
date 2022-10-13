package id.walt.verifier.backend

import id.walt.auditor.VerificationResult
import id.walt.vclib.credentials.VerifiablePresentation

data class VPVerificationResult(
    val vp: VerifiablePresentation,
    val verification_result: VerificationResult
)

data class SIOPResponseVerificationResult(
    val state: String,
    val subject: String?,
    val vps: List<VPVerificationResult>,
    var auth_token: String?
) {
    val isValid
        get() = !subject.isNullOrEmpty() && vps.isNotEmpty() && vps.all { vp -> (vp.verification_result.valid) }
}
