package id.walt.gateway.providers.metaco.restapi.ledger.model.fees.bitcoin

import id.walt.gateway.providers.metaco.restapi.ledger.model.fees.Fees
import kotlinx.serialization.Serializable

@Serializable
data class BitcoinFees(
    override val high: BitcoinPriorityStrategy,
    override val medium: BitcoinPriorityStrategy,
    override val low: BitcoinPriorityStrategy,
) : Fees("Bitcoin")
