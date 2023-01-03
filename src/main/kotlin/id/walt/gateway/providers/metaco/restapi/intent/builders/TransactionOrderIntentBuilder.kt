package id.walt.gateway.providers.metaco.restapi.intent.builders

import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.providers.metaco.restapi.intent.model.Author
import id.walt.gateway.providers.metaco.restapi.intent.model.NoSignatureIntent
import id.walt.gateway.providers.metaco.restapi.intent.model.Request
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.TransactionOrderPayload
import id.walt.gateway.providers.metaco.restapi.models.customproperties.CustomProperties
import id.walt.gateway.providers.metaco.restapi.models.customproperties.TransactionOrderTypeCustomProperties
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class TransactionOrderIntentBuilder(
    private val parametersType: String,
) : IntentBuilder {
    override fun build(data: TradeData) = NoSignatureIntent(
        Request(
            //TODO: use another model for params to contain all required data (including domainId, userId, etc.)
            Author(domainId = ProviderConfig.domainId, id = ProviderConfig.userId),
            expiryAt = Instant.now().plus(Duration.ofHours(1)).truncatedTo(ChronoUnit.SECONDS).toString(),
            targetDomainId = ProviderConfig.domainId,
            id = UUID.randomUUID().toString(),
            payload = TransactionOrderPayload(
                id = UUID.randomUUID().toString(),
                accountId = data.trade.sender,
                customProperties = TransactionOrderTypeCustomProperties(data.type),
                parameters = ParameterBuilder.getBuilder(parametersType).build(data.trade),
            ),
            customProperties = CustomProperties(),
            type = "Propose",
        )
    )
}