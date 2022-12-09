package id.walt.gateway.dto.trades

import kotlinx.serialization.Serializable

@Serializable
data class TradeValidationData(
    val result: Boolean,
    val message: String? = null,
)