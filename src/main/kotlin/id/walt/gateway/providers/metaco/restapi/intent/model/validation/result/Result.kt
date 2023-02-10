package id.walt.gateway.providers.metaco.restapi.intent.model.validation.result

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
@TypeFor(field = "type", adapter = ResultTypeAdapter::class)
sealed class Result(
    val type: String,
)

class ResultTypeAdapter : TypeAdapter<Result> {
    override fun classFor(type: Any): KClass<out Result> = when (type as String) {
        "Failure" -> FailureEstimate::class
        "Success" -> SuccessResult::class
        else -> throw IllegalArgumentException("Unknown validation result type: $type")
    }
}