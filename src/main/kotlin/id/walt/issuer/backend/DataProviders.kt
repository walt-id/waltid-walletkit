package id.walt.issuer.backend

import id.walt.signatory.ProofConfig
import id.walt.signatory.SignatoryDataProvider
import id.walt.signatory.dateFormat
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.vclist.VerifiableId
import java.util.*

class VerifiableIdDataProvider : SignatoryDataProvider {

  override fun populate(template: VerifiableCredential, proofConfig: ProofConfig): VerifiableId {
    val vc = template as VerifiableId

    vc.id = proofConfig.credentialId ?: "identity#verifiableID#${UUID.randomUUID()}"
    vc.issuer = proofConfig.issuerDid
    if (proofConfig.issueDate != null) vc.issuanceDate = dateFormat.format(proofConfig.issueDate)
    if (proofConfig.validDate != null) vc.validFrom = dateFormat.format(proofConfig.validDate)
    if (proofConfig.expirationDate != null) vc.expirationDate = dateFormat.format(proofConfig.expirationDate)
    vc.validFrom = vc.issuanceDate
    vc.credentialSubject!!.id = proofConfig.subjectDid
    vc.evidence!!.verifier = proofConfig.issuerDid

    return vc
  }
}