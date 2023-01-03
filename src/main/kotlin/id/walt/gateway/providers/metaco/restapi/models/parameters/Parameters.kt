package id.walt.gateway.providers.metaco.restapi.models.parameters

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import id.walt.gateway.providers.metaco.restapi.intent.model.fee.FeeStrategy
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
@TypeFor(field = "type", adapter = ParametersTypeAdapter::class)
sealed class Parameters(
    val type: String,
) {
    abstract val feeStrategy: FeeStrategy
    abstract val maximumFee: String
}

class ParametersTypeAdapter : TypeAdapter<Parameters> {
    override fun classFor(type: Any): KClass<out Parameters> =
        when (type as String) {
            "Ethereum" -> EthereumParameters::class
            "Bitcoin" -> BitcoinParameters::class
            else -> throw IllegalArgumentException("Unknown type: $type")
        }
}