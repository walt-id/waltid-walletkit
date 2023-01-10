package id.walt.gateway.dto

import kotlinx.serialization.Serializable

@Serializable
data class CoinData(
    val price: Double,
    val marketCap: Double,
    val change: Double,
)
