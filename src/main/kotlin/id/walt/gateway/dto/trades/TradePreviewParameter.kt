package id.walt.gateway.dto.trades

import kotlinx.serialization.Serializable

@Serializable
data class TradePreviewParameter(
    val amount: String,
    val maxFee: String,
    val ticker: String,
)