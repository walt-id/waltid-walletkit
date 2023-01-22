package id.walt.gateway.providers.metaco.restapi.ledger.model.fees.bitcoin

import id.walt.gateway.providers.metaco.restapi.ledger.model.fees.Fees
import kotlinx.serialization.Serializable

@Serializable
data class BitcoinFees(
    val high: LevelFee,
    val medium: LevelFee,
    val low: LevelFee,
) : Fees("Bitcoin")
