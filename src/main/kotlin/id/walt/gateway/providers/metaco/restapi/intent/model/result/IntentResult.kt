package id.walt.gateway.providers.metaco.restapi.intent.model.result

import kotlinx.serialization.Serializable

@Serializable
data class IntentResult(
    val message: String? = null,
    val reason: String? = null,
    val requestId: String? = null,
)
