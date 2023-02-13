package id.walt.gateway.dto.exchanges

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeParameter(
    val amount: String,
    val from: String,
    val to: String,
    val type: String,
)