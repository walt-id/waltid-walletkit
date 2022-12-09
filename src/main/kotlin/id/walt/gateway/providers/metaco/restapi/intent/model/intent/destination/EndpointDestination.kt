package id.walt.gateway.providers.metaco.restapi.intent.model.intent.destination

import kotlinx.serialization.Serializable

@Serializable
data class EndpointDestination(
    val endpointId: String,
) : Destination() {
    override val type = "Endpoint"
}