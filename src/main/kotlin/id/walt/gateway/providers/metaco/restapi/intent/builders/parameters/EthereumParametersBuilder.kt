package id.walt.gateway.providers.metaco.restapi.intent.builders.parameters

import id.walt.gateway.dto.trades.TransferParameter
import id.walt.gateway.providers.metaco.restapi.intent.model.fee.PriorityFeeStrategy
import id.walt.gateway.providers.metaco.restapi.models.destination.Destination
import id.walt.gateway.providers.metaco.restapi.models.parameters.EthereumParameters
import id.walt.gateway.providers.metaco.restapi.models.parameters.Parameters

class EthereumParametersBuilder : ParameterBuilder {
    override fun build(params: TransferParameter): Parameters = EthereumParameters(
        amount = params.amount,
        maximumFee = params.maxFee,
        feeStrategy = PriorityFeeStrategy("High"),//TODO: fix hard-code
        destination = Destination.parse(params.recipient.accountId)
    )
}