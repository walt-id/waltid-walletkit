package id.walt.gateway.dto

import kotlinx.serialization.Serializable

@Serializable
data class ValueWithChange(
    val value: String = "*",
    val change: String = "*",
    val reference: String = "24h",
)
