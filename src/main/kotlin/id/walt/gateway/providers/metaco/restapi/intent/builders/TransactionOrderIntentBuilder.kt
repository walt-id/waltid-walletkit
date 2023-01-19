package id.walt.gateway.providers.metaco.restapi.intent.builders

import id.walt.gateway.dto.intents.IntentParameter
import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.providers.metaco.restapi.intent.model.Author
import id.walt.gateway.providers.metaco.restapi.intent.model.NoSignatureIntent
import id.walt.gateway.providers.metaco.restapi.intent.model.Request
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.TransactionOrderPayload
import id.walt.gateway.providers.metaco.restapi.models.customproperties.CustomProperties
import id.walt.gateway.providers.metaco.restapi.models.customproperties.TransactionOrderTypeCustomProperties
import java.util.*

class TransactionOrderIntentBuilder(
    private val parametersType: String,
) : IntentBuilder<TradeData> {
    override fun build(parameter: IntentParameter<TradeData>) = NoSignatureIntent(
        Request(
            author = Author(domainId = parameter.author.domainId, id = parameter.author.userId),
            expiryAt = parameter.expiry.toString(),
            targetDomainId = parameter.data.trade.sender.domainId,
            id = UUID.randomUUID().toString(),
            payload = TransactionOrderPayload(
                id = UUID.randomUUID().toString(),
                accountId = parameter.data.trade.sender.accountId,
                customProperties = TransactionOrderTypeCustomProperties(parameter.data.type),
                parameters = ParameterBuilder.getBuilder(parametersType).build(parameter.data.trade),
            ),
            customProperties = CustomProperties(),
            type = parameter.type,
        )
    )
}