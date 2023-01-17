package id.walt.gateway.providers.metaco.restapi.models.metadata

import kotlinx.serialization.Serializable

@Serializable
data class LastModifiedBy(
    val domainId: String,
    val id: String
)