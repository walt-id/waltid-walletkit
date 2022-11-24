package id.walt.gateway.providers.metaco.restapi.order.model

import kotlinx.serialization.Serializable

@Serializable
data class FeeStrategy(
    val priority: String,
    val type: String
)