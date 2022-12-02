package id.walt.gateway.dto.trades

import kotlinx.serialization.Serializable

@Serializable
data class BuyParameter(
    val from: String,
    val amount: String,
)