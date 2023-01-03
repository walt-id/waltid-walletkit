package id.walt.gateway.providers.metaco.restapi.transfer.model

import kotlinx.serialization.Serializable

@Serializable
data class ResolvedEndpoint(
    val endpointId: String,
    val domainId: String,
)