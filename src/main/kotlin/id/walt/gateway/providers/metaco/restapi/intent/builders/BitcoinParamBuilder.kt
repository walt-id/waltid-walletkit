package id.walt.gateway.providers.metaco.restapi.intent.builders

import id.walt.gateway.dto.trades.TradeParameter
import id.walt.gateway.providers.metaco.restapi.intent.model.Output
import id.walt.gateway.providers.metaco.restapi.intent.model.destination.Destination
import id.walt.gateway.providers.metaco.restapi.intent.model.fee.PriorityFeeStrategy
import id.walt.gateway.providers.metaco.restapi.intent.model.parameters.BitcoinParameters
import id.walt.gateway.providers.metaco.restapi.intent.model.parameters.Parameters

class BitcoinParamBuilder : ParameterBuilder {
    override fun build(params: TradeParameter): Parameters = BitcoinParameters(
        feeStrategy = PriorityFeeStrategy("Medium"),//TODO: fix hard-code
        maximumFee = params.maxFee,
        outputs = listOf(
            Output(
                amount = params.amount,
                paysFee = false,
                destination = Destination.parse(params.recipient)
            )
        )
    )
}