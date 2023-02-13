package id.walt.gateway.providers.metaco.restapi.intent.builders.payload

import com.beust.klaxon.Klaxon
import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.providers.metaco.restapi.intent.builders.parameters.ParameterBuilder
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.TransactionOrderPayload
import id.walt.gateway.providers.metaco.restapi.models.customproperties.TransactionCustomProperties
import java.util.*

class TransactionOrderPayloadBuilder(
    private val parametersType: String,
    private val additionalInfo: Map<String, String>,
) : PayloadBuilder<TradeData> {
    override fun build(data: TradeData) = TransactionOrderPayload(
        id = UUID.randomUUID().toString(),
        accountId = data.trade.sender.accountId,
        customProperties = mapOf(
            "transactionProperties" to
                    Klaxon().toJsonString(
                        TransactionCustomProperties(
                            value = additionalInfo["value"] ?: "0",
                            change = additionalInfo["change"] ?: "0",
                            currency = additionalInfo["currency"] ?: "*",
                            tokenPrice = additionalInfo["tokenPrice"] ?: "0",
                            tokenSymbol = additionalInfo["tokenSymbol"] ?: "*",
                            tokenDecimals = additionalInfo["tokenDecimals"] ?: "0",
                            type = data.type
                        )
                    ),
        ),
        parameters = ParameterBuilder.getBuilder(parametersType).build(data.trade),
    )
}