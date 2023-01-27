package id.walt.gateway.dto.coins

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class CoinData(
    val askPrice: Double,
    @Json(serializeNull = false)
    val bidPrice: Double? = null,
    val marketCap: Double,
    val change: Double,
)
