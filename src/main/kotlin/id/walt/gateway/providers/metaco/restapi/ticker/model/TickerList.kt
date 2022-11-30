package id.walt.gateway.providers.metaco.restapi.ticker.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.ticker.model.Ticker
import kotlinx.serialization.Serializable

@Serializable
data class TickerList(
    val items: List<Ticker>,
    val count: Int,
    @Json(serializeNull = false)
    val currentStartingAfter: String?,
    @Json(serializeNull = false)
    val nextStartingAfter: String?
)