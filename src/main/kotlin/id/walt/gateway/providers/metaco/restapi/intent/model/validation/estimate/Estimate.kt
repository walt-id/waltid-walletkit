package id.walt.gateway.providers.metaco.restapi.intent.model.validation.estimate

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
@TypeFor(field = "type", adapter = EstimateTypeAdapter::class)
sealed class Estimate(
    val type: String,
)

class EstimateTypeAdapter : TypeAdapter<Estimate> {
    override fun classFor(type: Any): KClass<out Estimate> = when (type as String) {
        "Failure" -> FailureEstimate::class
        "Success" -> SuccessEstimate::class
        else -> throw IllegalArgumentException("Unknown estimate type: $type")
    }
}