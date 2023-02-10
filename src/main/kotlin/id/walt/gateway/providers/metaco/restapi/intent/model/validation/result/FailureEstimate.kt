package id.walt.gateway.providers.metaco.restapi.intent.model.validation.result

import kotlinx.serialization.Serializable

@Serializable
class FailureEstimate(
    val hint: String,
    val reason: String,
) : Result("Failure")