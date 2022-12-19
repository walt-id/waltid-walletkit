package id.walt.gateway.providers.metaco.restapi.ticker.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class Ticker(
    val data: Data,
    val kind: String,
    val ledgerDetails: LedgerDetails,
    @Json(serializeNull = false)
    val signature: String?,
)