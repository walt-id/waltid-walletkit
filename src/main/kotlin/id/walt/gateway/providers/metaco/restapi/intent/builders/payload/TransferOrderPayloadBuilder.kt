package id.walt.gateway.providers.metaco.restapi.intent.builders.payload

import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.TransferOrderPayload
import id.walt.gateway.providers.metaco.restapi.models.customproperties.OrderTransactionTypeCustomProperties
import id.walt.gateway.providers.metaco.restapi.models.destination.Destination
import id.walt.gateway.providers.metaco.restapi.models.parameters.Output
import java.util.*

class TransferOrderPayloadBuilder : PayloadBuilder<TradeData> {
    override fun build(data: TradeData): Payload = TransferOrderPayload(
        id = UUID.randomUUID().toString(),
        accountId = data.trade.sender.accountId,
        tickerId = data.trade.ticker,
        outputs = listOf(
            Output(
                destination = Destination.parse(data.trade.recipient.accountId),
                amount = data.trade.amount,
                paysFee = false,
            )
        ),
        feeStrategy = "Medium", //TODO: fix hardcoded value
        maximumFee = data.trade.maxFee,
        customProperties = OrderTransactionTypeCustomProperties(data.type),
    )
}