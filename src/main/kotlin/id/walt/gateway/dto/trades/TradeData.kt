package id.walt.gateway.dto.trades

import kotlinx.serialization.Serializable

@Serializable
data class TradeData(
    val trade: TransferParameter,
    val type: String,
)
