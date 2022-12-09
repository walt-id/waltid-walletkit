package id.walt.gateway.providers.metaco.restapi.intent.model.intent

import id.walt.gateway.providers.metaco.restapi.intent.model.intent.destination.Destination
import kotlinx.serialization.Serializable

@Serializable
data class Output(
    val amount: String,
    val destination: Destination,
    val paysFee: Boolean
)