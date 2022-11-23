package id.walt.gateway.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransferParameter(
    val to: String,
    val amount: String,
)
