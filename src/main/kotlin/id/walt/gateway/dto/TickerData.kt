package id.walt.gateway.dto

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
    val maxFee: Int,
)
