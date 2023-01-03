package id.walt.gateway.providers.metaco.restapi.models.destination

import kotlinx.serialization.Serializable

@Serializable
data class EndpointDestination(
    val endpointId: String,
) : Destination() {
    override val type = "Endpoint"
}