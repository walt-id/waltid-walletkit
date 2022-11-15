package id.walt.gateway.providers.metaco.restapi.ticker.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.ticker.model.Data
import id.walt.gateway.providers.metaco.restapi.ticker.model.LedgerDetails
import kotlinx.serialization.Serializable

@Serializable
data class Ticker(
    @Json(serializeNull = false)
    val data: Data?,
    val decimals: Int,
    val id: String,
    val kind: String,
    val ledgerDetails: LedgerDetails,
    val ledgerId: String,
    @Json(serializeNull = false)
    val name: String?,
    @Json(serializeNull = false)
    val signature: String?,
    val symbol: String
)