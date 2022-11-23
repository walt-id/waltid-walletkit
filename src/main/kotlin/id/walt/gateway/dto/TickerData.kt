package id.walt.gateway.dto

import kotlinx.serialization.Serializable

@Serializable
data class TickerData(
    val name: String,
    val price: ValueWithChange = ValueWithChange(),
    val imageUrl: String? = null,
)
