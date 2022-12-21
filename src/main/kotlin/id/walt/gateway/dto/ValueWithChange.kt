package id.walt.gateway.dto

import kotlinx.serialization.Serializable

@Serializable
data class ValueWithChange(
    val value: Double = .0,//Double.NaN,
    val change: Double = .0,//Double.NaN,
    val currency: String = "*",
    val reference: String = "24h",
)
