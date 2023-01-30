package id.walt.gateway.providers.metaco.restapi.models.customproperties

import kotlinx.serialization.Serializable

@Serializable
data class EnhancedDestinationReferenceCustomProperties(
    val accountId: String,
    val type: String,
)