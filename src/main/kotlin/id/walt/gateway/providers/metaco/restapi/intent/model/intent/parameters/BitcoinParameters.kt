package id.walt.gateway.providers.metaco.restapi.intent.model.intent.parameters

import id.walt.gateway.providers.metaco.restapi.intent.model.intent.Output
import id.walt.gateway.providers.metaco.restapi.intent.model.intent.fee.FeeStrategy
import kotlinx.serialization.Serializable

@Serializable
data class BitcoinParameters(
    val outputs: List<Output>,
    override val feeStrategy: FeeStrategy,
    override val maximumFee: String
) : Parameters("Bitcoin")