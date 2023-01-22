package id.walt.gateway.dto.tickers

import kotlinx.serialization.Serializable

@Serializable
data class FeeData(
    val fee: String,
    val level: String = "Medium",
)
