package id.walt.gateway.providers.metaco.restapi.transaction.model

import kotlinx.serialization.Serializable

@Serializable
data class RelatedAccount(
    val domainId: String,
    val id: String,
    val sender: Boolean
)