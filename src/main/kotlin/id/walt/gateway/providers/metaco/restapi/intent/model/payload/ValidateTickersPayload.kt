package id.walt.gateway.providers.metaco.restapi.intent.model.payload

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.models.customproperties.CustomProperties
import id.walt.gateway.providers.metaco.restapi.ticker.model.LedgerDetails

class ValidateTickersPayload(
    val tickers: List<TickerData>,
) : Payload("v0_ValidateTickers") {
    data class TickerData(
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
        val description: String? = null,
        val customProperties: CustomProperties
    )
}