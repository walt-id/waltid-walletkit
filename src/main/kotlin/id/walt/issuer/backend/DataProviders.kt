package id.walt.issuer.backend

import id.walt.signatory.ProofConfig
import id.walt.signatory.SignatoryDataProvider
import id.walt.signatory.dateFormat
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.vclist.VerifiableDiploma
import id.walt.vclib.vclist.VerifiableId
import java.util.*

class IssuanceRequestDataProvider(
  val issuanceRequest: IssuanceRequest
) : SignatoryDataProvider {

  override fun populate(template: VerifiableCredential, proofConfig: ProofConfig): VerifiableCredential =
    when(template) {
    is VerifiableId -> populateVerifiableId(template, proofConfig)
    is VerifiableDiploma -> populateVerifiableDiploma(template, proofConfig)
    else -> template
  }

  fun populateVerifiableId(vc: VerifiableId, proofConfig: ProofConfig): VerifiableId {
    vc.id = proofConfig.credentialId ?: "identity#verifiableID#${UUID.randomUUID()}"
    vc.issuer = proofConfig.issuerDid
    if (proofConfig.issueDate != null) vc.issuanceDate = dateFormat.format(proofConfig.issueDate)
    if (proofConfig.validDate != null) vc.validFrom = dateFormat.format(proofConfig.validDate)
    if (proofConfig.expirationDate != null) vc.expirationDate = dateFormat.format(proofConfig.expirationDate)
    vc.validFrom = vc.issuanceDate
    vc.credentialSubject!!.id = proofConfig.subjectDid
    vc.evidence!!.verifier = proofConfig.issuerDid

    vc.credentialSubject!!.firstName = issuanceRequest.params["firstName"]?.firstOrNull()
    vc.credentialSubject!!.familyName = issuanceRequest.params["familyName"]?.firstOrNull()
    vc.credentialSubject!!.dateOfBirth = issuanceRequest.params["dateOfBirth"]?.firstOrNull()
    vc.credentialSubject!!.gender = issuanceRequest.params["gender"]?.firstOrNull()
    vc.credentialSubject!!.placeOfBirth = issuanceRequest.params["placeOfBirth"]?.firstOrNull()
    vc.credentialSubject!!.currentAddress = issuanceRequest.params["currentAddress"]?.firstOrNull()

    return vc
  }

  fun populateVerifiableDiploma(vc: VerifiableDiploma, proofConfig: ProofConfig): VerifiableDiploma {
    vc.id = proofConfig.credentialId ?: "education#higherEducation#${UUID.randomUUID()}"
    vc.issuer = proofConfig.issuerDid
    if (proofConfig.issueDate != null) vc.issuanceDate = dateFormat.format(proofConfig.issueDate)
    if (proofConfig.validDate != null) vc.validFrom = dateFormat.format(proofConfig.validDate)
    if (proofConfig.expirationDate != null) vc.expirationDate = dateFormat.format(proofConfig.expirationDate)
    vc.credentialSubject!!.id = proofConfig.subjectDid
    vc.credentialSubject!!.awardingOpportunity!!.awardingBody.id = proofConfig.issuerDid

    vc.credentialSubject!!.apply {
      familyName = issuanceRequest.params["familyName"]?.firstOrNull()
      givenNames = issuanceRequest.params["firstName"]?.firstOrNull()
      dateOfBirth = issuanceRequest.params["dateOfBirth"]?.firstOrNull()

      learningSpecification?.apply {
        ectsCreditPoints = issuanceRequest.params["ectsCreditPoints"]?.firstOrNull()?.toIntOrNull()
        eqfLevel = issuanceRequest.params["eqfLevel"]?.firstOrNull()?.toIntOrNull()
        iscedfCode = issuanceRequest.params["iscedfCode"]?.firstOrNull()?.let{ listOf(it) } ?: listOf()
        nqfLevel = issuanceRequest.params["nqfLevel"]?.firstOrNull()?.let { listOf(it) } ?: listOf()
      }
    }

    return vc
  }
}