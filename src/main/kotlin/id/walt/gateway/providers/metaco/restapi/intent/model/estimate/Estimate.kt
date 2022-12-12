package id.walt.gateway.providers.metaco.restapi.intent.model.estimate

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
//abstract class Estimate{
//    abstract val type: String
//}

//object EstimateSerializer : JsonContentPolymorphicSerializer<Estimate>(Estimate::class) {
//    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out Estimate> {
//        return when (element.jsonObject["type"]?.jsonPrimitive?.content) {
//            "Ethereum" -> EthereumEstimate.serializer()
//            else -> throw Exception("Unknown type: ${element.jsonObject["type"]}")
//        }
//    }
//}

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
            "Failure" -> FailureEstimate::class
            else -> throw IllegalArgumentException("Unknown type: $type")
        }
}