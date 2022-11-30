package id.walt.gateway.providers.metaco.restapi.order.model

import kotlinx.serialization.Serializable

@Serializable
data class Destination(
    val accountId: String,
    val type: String
)