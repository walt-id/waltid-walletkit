package id.walt.gateway.dto.trades

import kotlinx.serialization.Serializable

@Serializable
class TradeParameter(
    val amount: String,
    val ticker: String,
    val maxFee: String,
    val sender: String,
)
