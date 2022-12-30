package id.walt.gateway.dto.trades

import kotlinx.serialization.Serializable

@Serializable
data class TradeResult(
    val result: Boolean,
    val message: String? = null,
)
