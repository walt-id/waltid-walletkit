package id.walt.gateway.providers.metaco.restapi.intent.model.intent.fee

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
@TypeFor(field = "type", adapter = FeeTypeAdapter::class)
abstract class FeeStrategy(
    val type: String
)

class FeeTypeAdapter : TypeAdapter<FeeStrategy> {
    override fun classFor(type: Any): KClass<out FeeStrategy> =
        when (type as String) {
            "Priority" -> PriorityFeeStrategy::class
            "SpecifiedRate" -> SpecifiedRateFeeStrategy::class
            else -> throw IllegalArgumentException("Unknown type: $type")
        }
}