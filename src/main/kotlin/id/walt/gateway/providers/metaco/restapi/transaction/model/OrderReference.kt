package id.walt.gateway.providers.metaco.restapi.transaction.model

import kotlinx.serialization.Serializable

@Serializable
data class OrderReference(
    val id: String,
    val domainId: String
)