package id.walt.gateway.dto

data class IntentParameter(
    val amount: String,
    val maxFee: String,
    val type: String,
    val sender: String,
    val recipient: String,
)
