package id.walt.gateway.providers.metaco.restapi.transaction.model

import kotlinx.serialization.Serializable

@Serializable
data class OrderReference(
    val domainId: String,
    val id: String
)