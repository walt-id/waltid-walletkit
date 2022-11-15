package id.walt.gateway.providers.metaco.restapi.account.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class AdditionalDetails(
    @Json(serializeNull = false)
    val lastBalancesUpdateProcessedAt: String?,
    @Json(serializeNull = false)
    val lastBalancesUpdateRequestedAt: String?,
    val processing: Processing
)