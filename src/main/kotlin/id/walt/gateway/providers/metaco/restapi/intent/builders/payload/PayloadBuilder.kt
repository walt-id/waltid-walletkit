package id.walt.gateway.providers.metaco.restapi.intent.builders.payload

import id.walt.gateway.dto.CreateAccountPayloadData
import id.walt.gateway.dto.QuarantineTransferPayloadData
import id.walt.gateway.dto.intents.PayloadData
import id.walt.gateway.dto.intents.PayloadParameter
import id.walt.gateway.dto.tickers.TickerPayloadData
import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload

interface PayloadBuilder<T : PayloadData> {
    fun build(data: T): Payload

    companion object {
        fun <T : PayloadData> create(parameter: PayloadParameter<T>): Payload = when (parameter.type) {
            Payload.Types.CreateTransactionOrder.value -> TransactionOrderPayloadBuilder(parameter.parametersType, parameter.additionalInfo).build(parameter.data as TradeData)
            Payload.Types.CreateTransferOrder.value -> TransferOrderPayloadBuilder(parameter.additionalInfo).build(parameter.data as TradeData)
            Payload.Types.ValidateTickers.value -> ValidateTickersPayloadBuilder().build(parameter.data as TickerPayloadData)
            Payload.Types.ReleaseQuarantinedTransfers.value -> ReleaseQuarantinedTransfersPayloadBuilder().build(parameter.data as QuarantineTransferPayloadData)
            Payload.Types.CreateAccount.value -> CreateAccountPayloadBuilder().build(parameter.data as CreateAccountPayloadData)
            else -> throw IllegalArgumentException("Unknown type: ${parameter.type}")
        }
    }
}