package id.walt.gateway.providers.metaco.restapi.intent.builders

import id.walt.gateway.dto.trades.TransferParameter
import id.walt.gateway.providers.metaco.restapi.intent.model.fee.PriorityFeeStrategy
import id.walt.gateway.providers.metaco.restapi.models.destination.Destination
import id.walt.gateway.providers.metaco.restapi.models.parameters.BitcoinParameters
import id.walt.gateway.providers.metaco.restapi.models.parameters.Output
import id.walt.gateway.providers.metaco.restapi.models.parameters.Parameters

class BitcoinParamBuilder : ParameterBuilder {
    override fun build(params: TransferParameter): Parameters = BitcoinParameters(
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