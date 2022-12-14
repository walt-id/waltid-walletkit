package id.walt.gateway.providers.metaco.repositories

import id.walt.gateway.providers.metaco.restapi.intent.model.SignatureIntent
import id.walt.gateway.providers.metaco.restapi.intent.model.ValidationResponse
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import id.walt.gateway.providers.metaco.restapi.intent.model.result.IntentResult

interface IntentRepository {
    fun create(domainId: String, intent: SignatureIntent): IntentResult
    fun validate(domainId: String, payload: Payload): ValidationResponse
}