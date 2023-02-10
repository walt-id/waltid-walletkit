package id.walt.gateway.providers.metaco.restapi.intent.model.estimate

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
@TypeFor(field = "type", adapter = EstimateTypeAdapter::class)
abstract class Estimate(
    val type: String
)

class EstimateTypeAdapter : TypeAdapter<Estimate> {
    override fun classFor(type: Any): KClass<out Estimate> =
        when (type as String) {
            "Ethereum" -> EthereumEstimate::class
            "Bitcoin" -> BitcoinEstimate::class
            else -> throw IllegalArgumentException("Unknown type: $type")
        }
}