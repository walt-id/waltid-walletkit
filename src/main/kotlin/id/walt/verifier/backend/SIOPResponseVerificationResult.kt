package id.walt.verifier.backend

import id.walt.auditor.VerificationResult
import id.walt.common.SingleVCObject
import id.walt.common.VCObjectList
import id.walt.credentials.w3c.VerifiableCredential
import id.walt.credentials.w3c.VerifiablePresentation

data class VPVerificationResult(
    @SingleVCObject val vp: VerifiablePresentation,
    @VCObjectList val vcs: List<VerifiableCredential>,
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
