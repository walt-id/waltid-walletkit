package id.walt.gateway.dto.tickers

import com.beust.klaxon.Json
import id.walt.gateway.dto.ValueWithChange
import kotlinx.serialization.Serializable

@Serializable
data class TickerData(
    val id: String,
    val name: String,
    val kind: String,
    val chain: String,
    val price: ValueWithChange,
    val imageUrl: String? = null,
    val decimals: Int,
    val symbol: String,
    val maxFee: Long,
    val type: String,
    @Json(serializeNull = false)
    val address: String? = null,
)
