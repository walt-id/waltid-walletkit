package id.walt.gateway.providers.metaco.restapi.intent.builders.payload

import id.walt.gateway.dto.intents.PayloadData
import id.walt.gateway.dto.intents.PayloadParameter
import id.walt.gateway.dto.tickers.TickerPayloadData
import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload

interface PayloadBuilder<T : PayloadData> {
    fun build(data: T): Payload

    companion object {
        fun <T : PayloadData> create(parameter: PayloadParameter<T>): Payload = when (parameter.type) {
            "v0_CreateTransactionOrder" -> TransactionOrderPayloadBuilder(parameter.parametersType!!).build(parameter.data as TradeData)
            "v0_CreateTransferOrder" -> TransferOrderPayloadBuilder().build(parameter.data as TradeData)
            "v0_ValidateTickers" -> ValidateTickersPayloadBuilder().build(parameter.data as TickerPayloadData)
            else -> throw IllegalArgumentException("Unknown type: ${parameter.type}")
        }
    }
}