package id.walt.gateway.providers.metaco.restapi.intent.builders.payload

import com.beust.klaxon.Klaxon
import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.TransferOrderPayload
import id.walt.gateway.providers.metaco.restapi.models.customproperties.TransactionCustomProperties
import id.walt.gateway.providers.metaco.restapi.models.destination.Destination
import id.walt.gateway.providers.metaco.restapi.models.parameters.Output
import java.util.*

class TransferOrderPayloadBuilder(
    private val additionalInfo: Map<String, String>,
) : PayloadBuilder<TradeData> {
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
        customProperties = mapOf(
            "transactionProperties" to
            Klaxon().toJsonString(TransactionCustomProperties(
                value = additionalInfo["value"] ?: "0",
                change = additionalInfo["change"] ?: "0",
                currency = additionalInfo["currency"] ?: "*",
                tokenPrice = additionalInfo["tokenPrice"] ?: "0",
                tokenSymbol = additionalInfo["tokenSymbol"] ?: "*",
                tokenDecimals = additionalInfo["tokenDecimals"] ?: "0",
                type = data.type,
            )),
        ),
    )
}