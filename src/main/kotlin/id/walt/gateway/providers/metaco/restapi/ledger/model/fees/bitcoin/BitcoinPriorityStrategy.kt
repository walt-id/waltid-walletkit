package id.walt.gateway.providers.metaco.restapi.ledger.model.fees.bitcoin

import id.walt.gateway.providers.metaco.restapi.ledger.model.fees.PriorityStrategy
import kotlinx.serialization.Serializable

@Serializable
data class BitcoinPriorityStrategy(
    val satoshiPerVbyte: String,
) : PriorityStrategy()
