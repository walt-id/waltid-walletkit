package id.walt.gateway.dto.trades

import id.walt.gateway.dto.intents.IntentData
import kotlinx.serialization.Serializable

@Serializable
data class TradeData(
    val trade: TransferParameter,
    val type: String,
) : IntentData
