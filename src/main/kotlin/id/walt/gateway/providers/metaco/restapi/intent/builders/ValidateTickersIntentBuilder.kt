package id.walt.gateway.providers.metaco.restapi.intent.builders

import id.walt.gateway.dto.intents.IntentParameter
import id.walt.gateway.dto.tickers.TickerIntentData
import id.walt.gateway.providers.metaco.restapi.intent.model.Author
import id.walt.gateway.providers.metaco.restapi.intent.model.Intent
import id.walt.gateway.providers.metaco.restapi.intent.model.NoSignatureIntent
import id.walt.gateway.providers.metaco.restapi.intent.model.Request
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.ValidateTickersPayload
import id.walt.gateway.providers.metaco.restapi.models.customproperties.CustomProperties
import java.util.*

class ValidateTickersIntentBuilder : IntentBuilder<TickerIntentData> {
    override fun build(parameter: IntentParameter<TickerIntentData>): Intent = NoSignatureIntent(
        Request(
            author = Author(domainId = parameter.author.domainId, id = parameter.author.userId),
            expiryAt = parameter.expiry.toString(),
            targetDomainId = parameter.data.targetDomainId,
            id = UUID.randomUUID().toString(),
            payload = ValidateTickersPayload(
                listOf(parameter.data.ticker)
            ),
            customProperties = CustomProperties(),
            type = parameter.type,
        )
    )
}