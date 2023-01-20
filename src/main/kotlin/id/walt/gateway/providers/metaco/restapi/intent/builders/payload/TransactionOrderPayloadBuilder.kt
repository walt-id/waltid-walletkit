package id.walt.gateway.providers.metaco.restapi.intent.builders.payload

import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.providers.metaco.restapi.intent.builders.parameters.ParameterBuilder
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.TransactionOrderPayload
import id.walt.gateway.providers.metaco.restapi.models.customproperties.TransactionOrderTypeCustomProperties
import java.util.*

class TransactionOrderPayloadBuilder(
    private val parametersType: String,
) : PayloadBuilder<TradeData> {
    override fun build(data: TradeData) = TransactionOrderPayload(
        id = UUID.randomUUID().toString(),
        accountId = data.trade.sender.accountId,
        customProperties = TransactionOrderTypeCustomProperties(data.type),
        parameters = ParameterBuilder.getBuilder(parametersType).build(data.trade),
    )
}