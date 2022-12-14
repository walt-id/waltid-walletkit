package id.walt.gateway.providers.metaco.restapi.intent.model.parameters

import id.walt.gateway.providers.metaco.restapi.intent.model.Output
import id.walt.gateway.providers.metaco.restapi.intent.model.fee.FeeStrategy
import kotlinx.serialization.Serializable

@Serializable
data class BitcoinParameters(
    val outputs: List<Output>,
    override val feeStrategy: FeeStrategy,
    override val maximumFee: String
) : Parameters("Bitcoin")