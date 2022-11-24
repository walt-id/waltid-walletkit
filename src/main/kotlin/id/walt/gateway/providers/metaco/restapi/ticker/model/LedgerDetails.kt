package id.walt.gateway.providers.metaco.restapi.ticker.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class LedgerDetails(
    @Json(serializeNull = false)
    val properties: Properties?,
    val type: String
)