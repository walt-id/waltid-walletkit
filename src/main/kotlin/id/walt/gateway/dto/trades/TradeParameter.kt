package id.walt.gateway.dto.trades

import id.walt.gateway.dto.accounts.AccountIdentifier
import kotlinx.serialization.Serializable

@Serializable
class TradeParameter(
    val amount: String,
    val ticker: String,
    val maxFee: String,
    val sender: AccountIdentifier,
)
