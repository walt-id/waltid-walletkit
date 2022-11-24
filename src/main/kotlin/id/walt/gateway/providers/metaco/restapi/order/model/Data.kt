package id.walt.gateway.providers.metaco.restapi.order.model

import kotlinx.serialization.Serializable

@Serializable
data class Data(
    val accountId: String,
    val domainId: String,
    val id: String,
    val metadata: Metadata,
    val parameters: Parameters
)