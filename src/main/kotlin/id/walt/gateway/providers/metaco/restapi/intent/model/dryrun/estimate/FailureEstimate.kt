package id.walt.gateway.providers.metaco.restapi.intent.model.dryrun.estimate

import kotlinx.serialization.Serializable

@Serializable
class FailureEstimate(
    val hint: String? = null,
    val reason: String? = null,
) : Estimate("Failure")