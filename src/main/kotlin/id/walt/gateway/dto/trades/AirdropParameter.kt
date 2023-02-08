package id.walt.gateway.dto.trades

import id.walt.gateway.dto.accounts.AccountIdentifier
import kotlinx.serialization.Serializable

@Serializable
data class AirdropParameter(
    val amount: String,
    val ticker: String,
    val maxFee: String,
    val sender: AccountIdentifier,
    val addresses: List<String>,
)
