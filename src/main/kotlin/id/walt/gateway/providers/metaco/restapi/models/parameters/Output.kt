package id.walt.gateway.providers.metaco.restapi.models.parameters

import id.walt.gateway.providers.metaco.restapi.models.destination.Destination
import kotlinx.serialization.Serializable

@Serializable
data class Output(
    val amount: String,
    val destination: Destination,
    val paysFee: Boolean
)