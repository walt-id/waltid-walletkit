package id.walt.gateway.providers.metaco.restapi.address.model

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val id: String,
    val address: String,
    val keyPath: String? = null,
    val createdAt: String,
    val scope: String,
)