package id.walt.gateway.providers.metaco.restapi.intent.model.validation

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.intent.model.estimate.Estimate
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import id.walt.gateway.providers.metaco.restapi.intent.model.validation.result.Result
import kotlinx.serialization.Serializable

@Serializable
data class TransferOrderValidationResult(
    override val success: Boolean,
    @Json(serializeNull = false)
    override val errors: List<String>? = null,
    val result: Result,
    val estimate: Estimate,
) : ValidationResult(Payload.Types.CreateTransferOrder.value)
