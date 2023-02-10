package id.walt.gateway.providers.metaco.restapi.intent.model.validation

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
@TypeFor(field = "type", adapter = ValidationResultTypeAdapter::class)
sealed class ValidationResult(
    val type: String,
) {
    abstract val success: Boolean
    abstract val errors: List<String>
}

class ValidationResultTypeAdapter : TypeAdapter<ValidationResult> {
    override fun classFor(type: Any): KClass<out ValidationResult> = when (type as String) {
        Payload.Types.CreateTransferOrder.value -> TransferOrderValidationResult::class
        else -> throw IllegalArgumentException("Unknown validation type: $type")
    }
}