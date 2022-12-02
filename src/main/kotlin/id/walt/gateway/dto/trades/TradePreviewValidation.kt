package id.walt.gateway.dto.trades

import kotlinx.serialization.Serializable

@Serializable
data class TradePreviewValidation(
    val result: Boolean,
    val message: String? = null,
)