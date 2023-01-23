package id.walt.gateway.providers.metaco.restapi.ledger.model.parameters

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
@TypeFor(field = "type", adapter = ParametersTypeAdapter::class)
abstract class Parameters(
    val type: String
)

class ParametersTypeAdapter : TypeAdapter<Parameters> {
    override fun classFor(type: Any): KClass<out Parameters> = when (type as String) {
        "Ethereum" -> EthereumParameters::class
        else -> throw IllegalArgumentException("No parameter for $type")
    }
}