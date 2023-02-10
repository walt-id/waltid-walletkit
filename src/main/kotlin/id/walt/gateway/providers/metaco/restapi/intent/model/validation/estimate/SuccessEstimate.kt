package id.walt.gateway.providers.metaco.restapi.intent.model.validation.estimate

import kotlinx.serialization.Serializable

@Serializable
data class SuccessEstimate(
    val fee: String,
) : Estimate("Success")
