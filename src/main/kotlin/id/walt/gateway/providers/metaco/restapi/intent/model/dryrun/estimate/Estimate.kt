package id.walt.gateway.providers.metaco.restapi.intent.model.dryrun.estimate

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.reflect.KClass

//@Serializable(with = EstimateSerializer::class)
@TypeFor(field = "type", adapter = EstimateTypeAdapter::class)
abstract class Estimate {
    abstract val hint: String?
    abstract val reason: String?
    abstract val type: String
}

object EstimateSerializer : JsonContentPolymorphicSerializer<Estimate>(Estimate::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out Estimate> {
        return when (element.jsonObject["type"]?.jsonPrimitive?.content) {
            "Ethereum" -> EthereumEstimate.serializer()
            "Bitcoin" -> BitcoinEstimate.serializer()
            else -> throw Exception("Unknown Module: key 'type' not found or does not matches any module type")
        }
    }
}

class EstimateTypeAdapter : TypeAdapter<Estimate> {
    override fun classFor(type: Any): KClass<out Estimate> =
        when (type as String) {
            "Ethereum" -> EthereumEstimate::class
            "Bitcoin" -> BitcoinEstimate::class
            else -> throw IllegalArgumentException("Unknown estimate type $type")
        }
}