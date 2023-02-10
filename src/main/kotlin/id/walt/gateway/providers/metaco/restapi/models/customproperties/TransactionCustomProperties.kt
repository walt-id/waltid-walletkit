package id.walt.gateway.providers.metaco.restapi.models.customproperties

import kotlinx.serialization.Serializable

@Serializable
data class TransactionCustomProperties(
    val value: String,
    val change: String,
    val currency: String,
    val tokenPrice: String,
    val type: String,
)
