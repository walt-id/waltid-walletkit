package id.walt.gateway.providers.metaco.restapi.ledger.model.fees.ethereum

import id.walt.gateway.providers.metaco.restapi.ledger.model.fees.PriorityStrategy
import kotlinx.serialization.Serializable

@Serializable
data class EthereumPriorityStrategy(
    val gasPrice: String,
) : PriorityStrategy()
