package id.walt.gateway.providers.metaco.restapi.account.model

import kotlinx.serialization.Serializable

@Serializable
data class LastModifiedBy(
    val domainId: String,
    val id: String
)