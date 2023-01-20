package id.walt.gateway.providers.metaco.restapi.intent.builders.parameters

import id.walt.gateway.dto.trades.TransferParameter
import id.walt.gateway.providers.metaco.restapi.intent.model.fee.PriorityFeeStrategy
import id.walt.gateway.providers.metaco.restapi.models.destination.Destination
import id.walt.gateway.providers.metaco.restapi.models.parameters.BitcoinParameters
import id.walt.gateway.providers.metaco.restapi.models.parameters.Output
import id.walt.gateway.providers.metaco.restapi.models.parameters.Parameters

class BitcoinParametersBuilder : ParameterBuilder {
    override fun build(params: TransferParameter): Parameters = BitcoinParameters(
        feeStrategy = PriorityFeeStrategy("Medium"),//TODO: fix hard-code
        maximumFee = params.maxFee,
        outputs = listOf(
            Output(
                amount = params.amount,
                paysFee = false,
                destination = Destination.parse(params.recipient.accountId)
            )
        )
    )
}