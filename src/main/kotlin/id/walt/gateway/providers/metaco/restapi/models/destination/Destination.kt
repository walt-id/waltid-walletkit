package id.walt.gateway.providers.metaco.restapi.models.destination

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
@TypeFor(field = "type", adapter = DestinationTypeAdapter::class)
abstract class Destination {
    abstract val type: String

    companion object {
        private val uuidRegex = Regex("[a-zA-Z0-9]{8}(-[a-zA-Z0-9]{4}){3}-[a-zA-Z0-9]{12}")

        //TODO: fix for endpoint-id
        fun parse(value: String): Destination = when (uuidRegex.matches(value)) {
            true -> AccountDestination(value)
            false -> AddressDestination(value)
        }
    }
}

class DestinationTypeAdapter : TypeAdapter<Destination> {
    override fun classFor(type: Any): KClass<out Destination> =
        when (type as String) {
            "Account" -> AccountDestination::class
            "Address" -> AddressDestination::class
            "Endpoint" -> EndpointDestination::class
            else -> throw IllegalArgumentException("Unknown type: $type")
        }
}
