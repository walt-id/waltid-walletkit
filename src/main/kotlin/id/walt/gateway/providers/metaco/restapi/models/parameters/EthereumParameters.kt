package id.walt.gateway.providers.metaco.restapi.models.parameters

import id.walt.gateway.providers.metaco.restapi.models.destination.Destination
import id.walt.gateway.providers.metaco.restapi.intent.model.fee.FeeStrategy
import kotlinx.serialization.Serializable

@Serializable
data class EthereumParameters(
    val destination: Destination,
    val amount: String,
    override val feeStrategy: FeeStrategy,
    override val maximumFee: String,
) : Parameters("Ethereum")