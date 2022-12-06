package id.walt.gateway.dto.trades

import kotlinx.serialization.Serializable

@Serializable
data class SellParameter(
    val to: String,
    val amount: String,
    val ticker: String,
    val maxFee: String,
)