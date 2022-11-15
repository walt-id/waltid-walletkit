package id.walt.gateway.providers.metaco.restapi.order.model

import kotlinx.serialization.Serializable

@Serializable
data class CreatedBy(
    val domainId: String,
    val id: String
)