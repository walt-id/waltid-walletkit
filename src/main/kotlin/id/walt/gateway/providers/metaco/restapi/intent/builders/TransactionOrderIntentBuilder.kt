package id.walt.gateway.providers.metaco.restapi.intent.builders

import id.walt.gateway.dto.trades.TradePreview
import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.providers.metaco.restapi.intent.model.Author
import id.walt.gateway.providers.metaco.restapi.intent.model.CustomProperties
import id.walt.gateway.providers.metaco.restapi.intent.model.NoSignatureIntent
import id.walt.gateway.providers.metaco.restapi.intent.model.Request
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.TransactionOrderPayload
import java.time.LocalDateTime
import java.util.*

class TransactionOrderIntentBuilder(
    private val parametersType: String,
) : IntentBuilder {
    override fun build(params: TradePreview) = NoSignatureIntent(
        Request(
            //TODO: use another model for params to contain all required data (including domainId, userId, etc.)
            Author(domainId = ProviderConfig.domainId, id = ProviderConfig.userId),
            expiryAt = LocalDateTime.now().plusHours(1).toString(),
            targetDomainId = ProviderConfig.domainId,
            id = UUID.randomUUID().toString(),
            payload = TransactionOrderPayload(
                id = UUID.randomUUID().toString(),
                accountId = params.sender,
                customProperties = CustomProperties(),
                parameters = ParameterBuilder.getBuilder(parametersType).build(params),
            ),
            customProperties = CustomProperties(),
            type = "Propose",
        )
    )
}