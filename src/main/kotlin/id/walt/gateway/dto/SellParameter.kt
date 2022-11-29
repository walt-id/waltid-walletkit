package id.walt.gateway.dto

import kotlinx.serialization.Serializable

@Serializable
data class SellParameter(
    val to: String,
    val amount: String,
)