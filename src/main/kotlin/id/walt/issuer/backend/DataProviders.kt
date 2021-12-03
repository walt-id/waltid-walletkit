package id.walt.issuer.backend

import id.walt.signatory.ProofConfig
import id.walt.signatory.SignatoryDataProvider
import id.walt.signatory.dateFormat
import id.walt.vclib.credentials.VerifiableDiploma
import id.walt.vclib.credentials.VerifiableId
import id.walt.vclib.credentials.VerifiableVaccinationCertificate
import id.walt.vclib.model.VerifiableCredential
import java.util.*

class IssuanceRequestDataProvider(
  val issuanceRequest: IssuanceRequest
) : SignatoryDataProvider {

  override fun populate(template: VerifiableCredential, proofConfig: ProofConfig): VerifiableCredential =
    when(template) {
    is VerifiableId -> populateVerifiableId(template, proofConfig)
    is VerifiableDiploma -> populateVerifiableDiploma(template, proofConfig)
      is VerifiableVaccinationCertificate -> populateVerifiableVaccinationCert(template, proofConfig)
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

  fun populateVerifiableVaccinationCert(vc: VerifiableVaccinationCertificate, proofConfig: ProofConfig): VerifiableVaccinationCertificate {
    vc.id = proofConfig.credentialId ?: "education#higherEducation#${UUID.randomUUID()}"
    vc.issuer = proofConfig.issuerDid
    if (proofConfig.issueDate != null) vc.issuanceDate = dateFormat.format(proofConfig.issueDate)
    if (proofConfig.validDate != null) vc.validFrom = dateFormat.format(proofConfig.validDate)
    if (proofConfig.expirationDate != null) vc.expirationDate = dateFormat.format(proofConfig.expirationDate)
    vc.credentialSubject!!.id = proofConfig.subjectDid

    vc.credentialSubject!!.apply {
      familyName = issuanceRequest.params["familyName"]?.firstOrNull()
      givenNames = issuanceRequest.params["firstName"]?.firstOrNull()
      dateOfBirth = issuanceRequest.params["dateOfBirth"]?.firstOrNull()
      personSex = issuanceRequest.params["gender"]?.firstOrNull()

      vaccinationProphylaxisInformation = listOf(
        VerifiableVaccinationCertificate.CredentialSubject.VaccinationProphylaxisInformation(
          diseaseOrAgentTargeted = VerifiableVaccinationCertificate.CredentialSubject.VaccinationProphylaxisInformation.DiseaseOrAgentTargeted(
            code = "",
            system = "",
            version = ""
          ),
          vaccineOrProphylaxis = issuanceRequest.params["vaccineOrProphylaxis"]?.firstOrNull(),
          vaccineMedicinalProduct = issuanceRequest.params["vaccineMedicinalProduct"]?.firstOrNull(),
          doseNumber = issuanceRequest.params["doseNumber"]?.firstOrNull(),
          totalSeriesOfDoses = issuanceRequest.params["totalSeriesOfDoses"]?.firstOrNull(),
          dateOfVaccination = issuanceRequest.params["dateOfVaccination"]?.firstOrNull(),
          administeringCentre = issuanceRequest.params["administeringCentre"]?.firstOrNull(),
          countryOfVaccination = issuanceRequest.params["countryOfVaccination"]?.firstOrNull()
        )
      )
    }

    return vc

  }
}