package id.walt.gateway.dto

import kotlinx.serialization.Serializable

@Serializable
data class HistoricalPriceData(
    val date: String,
    val price: String,
)
