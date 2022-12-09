package id.walt.gateway.providers.metaco.restapi.intent.model.intent

import id.walt.gateway.providers.metaco.restapi.intent.model.intent.parameters.Parameters
import kotlinx.serialization.Serializable

@Serializable
data class NoSignatureIntent(
    val accountId: String,
    val parameters: Parameters
)