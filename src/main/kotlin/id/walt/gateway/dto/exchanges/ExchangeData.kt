package id.walt.gateway.dto.exchanges

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeData(
    val amount: String,
    val unitPrice: String,
)
