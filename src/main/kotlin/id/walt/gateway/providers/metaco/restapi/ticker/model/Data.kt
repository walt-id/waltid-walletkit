package id.walt.gateway.providers.metaco.restapi.ticker.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    val id: String,
    val ledgerId: String,
    val kind: String,
    val name: String,
    @Json(serializeNull = false)
    val decimals: Int?,
    @Json(serializeNull = false)
    val symbol: String? = null,
    val ledgerDetails: LedgerDetails,
    val lock: String,
    @Json(serializeNull = false)
    val metadata: String? = null,
)