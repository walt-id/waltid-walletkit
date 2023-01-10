package id.walt.gateway.dto

import com.beust.klaxon.Json
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
    @Json(serializeNull = false)
    val address: String? = null,
)
