package id.walt.gateway.dto.coins

import com.beust.klaxon.Json
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@Serializable
data class CoinData(
    @JsonProperty("askprice")
    val askPrice: Double,
    @JsonProperty("bidprice")
    @Json(serializeNull = false)
    val bidPrice: Double? = null,
    @Json(serializeNull = false)
    val marketCap: Double? = null,
    val change: Double,
)
