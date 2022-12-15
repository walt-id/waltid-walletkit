package id.walt.gateway.providers.metaco.restapi.intent.model.destination

import kotlinx.serialization.Serializable

@Serializable
data class AddressDestination(
    val address: String,
) : Destination() {
    override val type = "Address"
}