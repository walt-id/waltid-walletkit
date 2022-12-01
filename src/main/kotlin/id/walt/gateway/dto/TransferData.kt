package id.walt.gateway.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransferData(
    val amount: String,
    val type: String,
    val address: String? = null,
)
