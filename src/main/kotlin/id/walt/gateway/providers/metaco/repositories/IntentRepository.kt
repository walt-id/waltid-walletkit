package id.walt.gateway.providers.metaco.repositories

import id.walt.gateway.providers.metaco.restapi.intent.model.Intent
import id.walt.gateway.providers.metaco.restapi.intent.model.SignatureIntent
import id.walt.gateway.providers.metaco.restapi.intent.model.result.IntentResult
import id.walt.gateway.providers.metaco.restapi.intent.model.validation.ValidationResult

interface IntentRepository {
    fun create(intent: SignatureIntent): IntentResult
    fun validate(intent: Intent): ValidationResult
}
