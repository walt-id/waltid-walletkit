package id.walt.gateway.providers.metaco.restapi.intent.model

import id.walt.gateway.providers.metaco.restapi.intent.model.destination.Destination
import kotlinx.serialization.Serializable

@Serializable
data class Output(
    val amount: String,
    val destination: Destination,
    val paysFee: Boolean
)