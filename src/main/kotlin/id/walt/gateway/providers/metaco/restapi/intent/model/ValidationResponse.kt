package id.walt.gateway.providers.metaco.restapi.intent.model

import id.walt.gateway.providers.metaco.restapi.intent.model.dryrun.Result
import id.walt.gateway.providers.metaco.restapi.intent.model.dryrun.estimate.Estimate
import kotlinx.serialization.Serializable

@Serializable
data class ValidationResponse(
    val result: Result,
    val estimate: Estimate
)