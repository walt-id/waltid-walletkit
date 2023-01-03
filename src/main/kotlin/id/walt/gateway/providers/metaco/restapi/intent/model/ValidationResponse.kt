package id.walt.gateway.providers.metaco.restapi.intent.model

import com.beust.klaxon.Json
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import id.walt.gateway.providers.metaco.restapi.intent.model.dryrun.Result
import id.walt.gateway.providers.metaco.restapi.intent.model.estimate.Estimate
import id.walt.gateway.providers.metaco.restapi.intent.model.estimate.EthereumEstimate
import kotlinx.serialization.Serializable

@Serializable
data class ValidationResponse(
    val result: Result,
    val estimate: Estimate
)