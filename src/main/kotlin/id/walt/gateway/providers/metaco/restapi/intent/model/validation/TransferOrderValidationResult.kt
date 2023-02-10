package id.walt.gateway.providers.metaco.restapi.intent.model.validation

import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import id.walt.gateway.providers.metaco.restapi.intent.model.validation.estimate.Estimate
import id.walt.gateway.providers.metaco.restapi.intent.model.validation.result.Result
import kotlinx.serialization.Serializable

@Serializable
data class TransferOrderValidationResult(
    override val success: Boolean,
    override val errors: List<String>,
    val result: Result,
    val estimate: Estimate,
) : ValidationResult(Payload.Types.CreateTransferOrder.value)
