package id.walt.webwallet.backend.wallet

import id.walt.model.siopv2.SIOPv2Request
import id.walt.vclib.model.VerifiableCredential

data class ClaimedCredential (
  val claimId: String,
  val credential: VerifiableCredential
    )

data class PresentationExchange (
  val subject: String,
  val request: SIOPv2Request,
  val claimedCredentials: List<ClaimedCredential>
    )

data class PresentationExchangeResponse(
  val id_token: String,
  val vp_token: String
)