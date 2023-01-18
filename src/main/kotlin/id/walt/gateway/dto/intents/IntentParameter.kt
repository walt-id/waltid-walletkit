package id.walt.gateway.dto.intents

import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.dto.users.UserIdentifier

data class IntentParameter(
    val data: TradeData,
    val author: UserIdentifier,
    val type: String = "Propose",
)
