package id.walt.gateway.providers.metaco.restapi.intent.model.estimate

import kotlinx.serialization.Serializable

@Serializable
data class FailureEstimate(
    val hint: String,
    val reason: String,
) : Estimate("Failure")
