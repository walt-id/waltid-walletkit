package id.walt.gateway.providers.metaco.restapi.ticker.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    val decimals: Long,
    val id: String,
    val kind: String,
    @Json(serializeNull = false)
    val ledgerDetails: LedgerDetails?,
    val ledgerId: String,
    val lock: String,
    @Json(serializeNull = false)
    val metadata: String?,
    val name: String,
    val symbol: String
)