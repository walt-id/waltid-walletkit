package id.walt.gateway.providers.metaco.restapi.intent.model

import kotlinx.serialization.Serializable

@Serializable
data class Author(
    val domainId: String,
    val id: String
)