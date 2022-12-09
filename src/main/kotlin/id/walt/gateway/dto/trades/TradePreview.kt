package id.walt.gateway.dto.trades

import kotlinx.serialization.Serializable

@Serializable
data class TradePreview(
    val amount: String,
    val maxFee: String,
    val ticker: String,
    val sender: String,
    val recipient: String,
)