package id.walt.gateway.providers.metaco.restapi.intent.model.intent.destination

import kotlinx.serialization.Serializable

@Serializable
abstract class Destination {
    abstract val type: String

    companion object{
        private val uuidRegex = Regex("[a-zA-Z0-9]{8}(-[a-zA-Z0-9]{4}){3}-[a-zA-Z0-9]{12}")

        //TODO: fix for endpoint-id
        fun parse(value: String): Destination = when (uuidRegex.matches(value)) {
            true -> AccountDestination(value)
            false -> AddressDestination(value)
        }
    }
}