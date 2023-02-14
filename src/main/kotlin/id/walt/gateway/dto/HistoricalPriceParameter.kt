package id.walt.gateway.dto

import kotlinx.serialization.Serializable

@Serializable
data class HistoricalPriceParameter(
    val timeframe: String,
    val asset: String,
)
