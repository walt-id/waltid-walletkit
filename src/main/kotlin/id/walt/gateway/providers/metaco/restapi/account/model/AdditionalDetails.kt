package id.walt.gateway.providers.metaco.restapi.account.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class AdditionalDetails(
    @Json(serializeNull = false)
    val lastBalancesUpdateProcessedAt: String? = null,
    @Json(serializeNull = false)
    val lastBalancesUpdateRequestedAt: String? = null,
    val processing: Processing
)