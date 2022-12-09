package id.walt.gateway.providers.metaco.repositories

import id.walt.gateway.providers.metaco.restapi.intent.model.IntentResult
import id.walt.gateway.providers.metaco.restapi.intent.model.ValidationResponse
import id.walt.gateway.providers.metaco.restapi.intent.model.intent.NoSignatureIntent

interface IntentRepository {
    fun create(domainId: String): IntentResult
    fun validate(domainId: String, intent: NoSignatureIntent): ValidationResponse
}