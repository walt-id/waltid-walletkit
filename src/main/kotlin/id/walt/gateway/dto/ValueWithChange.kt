package id.walt.gateway.dto

import kotlinx.serialization.Serializable

@Serializable
data class ValueWithChange(
    val value: Double = Double.NaN,
    val change: Double = Double.NaN,
    val reference: String = "24h",
)
