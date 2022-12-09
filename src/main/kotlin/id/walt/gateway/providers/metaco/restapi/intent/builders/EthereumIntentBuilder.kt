package id.walt.gateway.providers.metaco.restapi.intent.builders

import id.walt.gateway.dto.trades.TradePreview
import id.walt.gateway.providers.metaco.restapi.intent.model.intent.NoSignatureIntent
import id.walt.gateway.providers.metaco.restapi.intent.model.intent.destination.Destination
import id.walt.gateway.providers.metaco.restapi.intent.model.intent.fee.PriorityFeeStrategy
import id.walt.gateway.providers.metaco.restapi.intent.model.intent.parameters.EthereumParameters

class EthereumIntentBuilder : IntentBuilder {
    override fun build(params: TradePreview) = NoSignatureIntent(
        accountId = params.sender,
        parameters = EthereumParameters(
            amount = params.amount,
            maximumFee = params.maxFee,
            feeStrategy = PriorityFeeStrategy("Medium"),//TODO: fix hard-code
            destination = Destination.parse(params.recipient)
        )
    )
}