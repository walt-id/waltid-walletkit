package id.walt.gateway.providers.metaco.restapi.intent.builders

import id.walt.gateway.dto.intents.IntentParameter
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
    override fun build(parameter: IntentParameter) = NoSignatureIntent(
        Request(
            author = Author(domainId = parameter.author.domainId, id = parameter.author.userId),
            expiryAt = Instant.now().plus(Duration.ofHours(1)).truncatedTo(ChronoUnit.SECONDS).toString(),
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