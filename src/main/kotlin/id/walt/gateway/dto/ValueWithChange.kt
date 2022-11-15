package id.walt.gateway.dto

data class ValueWithChange(
    val value: String,
    val change: String,
    val reference: String = "24h",
)
