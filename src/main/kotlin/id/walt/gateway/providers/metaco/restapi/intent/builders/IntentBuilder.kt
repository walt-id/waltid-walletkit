package id.walt.gateway.providers.metaco.restapi.intent.builders

import id.walt.gateway.dto.intents.IntentParameter
import id.walt.gateway.providers.metaco.restapi.intent.model.Author
import id.walt.gateway.providers.metaco.restapi.intent.model.Intent
import id.walt.gateway.providers.metaco.restapi.intent.model.NoSignatureIntent
import id.walt.gateway.providers.metaco.restapi.intent.model.Request
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import java.util.*

object IntentBuilder {
    fun build(parameter: IntentParameter, payload: Payload): Intent = NoSignatureIntent(
        Request(
            author = Author(domainId = parameter.author.domainId, id = parameter.author.userId),
            expiryAt = parameter.expiry.toString(),
            targetDomainId = parameter.targetDomainId,
            id = UUID.randomUUID().toString(),
            payload = payload,
            customProperties = emptyMap(),
            type = parameter.type,
        )
    )
}