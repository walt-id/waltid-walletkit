package id.walt.gateway.dto.trades

import kotlinx.serialization.Serializable

@Serializable
data class SendParameter(
    val to: String,
    val amount: String,
)