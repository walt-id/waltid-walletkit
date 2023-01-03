package id.walt.gateway.dto.trades

import kotlinx.serialization.Serializable

@Serializable
data class TradeParameter(
    val amount: String,
    val ticker: String,
    val maxFee: String,
    val sender: String,
    val recipient: String,
)