package id.walt.gateway.providers.metaco.restapi.order.model

import id.walt.gateway.providers.metaco.restapi.models.metadata.Metadata
import id.walt.gateway.providers.metaco.restapi.models.parameters.Parameters
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    val accountId: String,
    val domainId: String,
    val id: String,
    val metadata: Metadata,
    val parameters: Parameters
)