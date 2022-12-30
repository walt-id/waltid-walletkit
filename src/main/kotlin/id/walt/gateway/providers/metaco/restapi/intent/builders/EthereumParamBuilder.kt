package id.walt.gateway.providers.metaco.restapi.intent.builders

import id.walt.gateway.dto.trades.TradeParameter
import id.walt.gateway.providers.metaco.restapi.models.destination.Destination
import id.walt.gateway.providers.metaco.restapi.intent.model.fee.PriorityFeeStrategy
import id.walt.gateway.providers.metaco.restapi.models.parameters.EthereumParameters
import id.walt.gateway.providers.metaco.restapi.models.parameters.Parameters

class EthereumParamBuilder : ParameterBuilder {
    override fun build(params: TradeParameter): Parameters = EthereumParameters(
        amount = params.amount,
        maximumFee = params.maxFee,
        feeStrategy = PriorityFeeStrategy("Medium"),//TODO: fix hard-code
        destination = Destination.parse(params.recipient)
    )
}