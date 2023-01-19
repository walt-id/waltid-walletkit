package id.walt.gateway.providers.metaco.restapi.intent.builders

import id.walt.gateway.dto.intents.IntentParameter
import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.providers.metaco.restapi.intent.model.Author
import id.walt.gateway.providers.metaco.restapi.intent.model.Intent
import id.walt.gateway.providers.metaco.restapi.intent.model.NoSignatureIntent
import id.walt.gateway.providers.metaco.restapi.intent.model.Request
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.TransferOrderPayload
import id.walt.gateway.providers.metaco.restapi.models.customproperties.CustomProperties
import id.walt.gateway.providers.metaco.restapi.models.destination.Destination
import id.walt.gateway.providers.metaco.restapi.models.parameters.Output
import java.util.*

class TransferOrderIntentBuilder : IntentBuilder<TradeData> {
    override fun build(parameter: IntentParameter<TradeData>): Intent = NoSignatureIntent(
        Request(
            author = Author(domainId = parameter.author.domainId, id = parameter.author.userId),
            expiryAt = parameter.expiry.toString(),
            targetDomainId = parameter.data.trade.sender.domainId,
            id = UUID.randomUUID().toString(),
            payload = TransferOrderPayload(
                id = UUID.randomUUID().toString(),
                accountId = parameter.data.trade.sender.accountId,
                tickerId = parameter.data.trade.ticker,
                outputs = listOf(
                    Output(
                        destination = Destination.parse(parameter.data.trade.recipient.accountId),
                        amount = parameter.data.trade.amount,
                        paysFee = false,
                    )
                ),
                feeStrategy = "Medium", //TODO: fix hardcoded value
                maximumFee = parameter.data.trade.maxFee,
                customProperties = CustomProperties(),
            ),
            customProperties = CustomProperties(),
            type = parameter.type,
        )
    )
}