package id.walt.gateway.dto.tickers

import kotlinx.serialization.Serializable

@Serializable
data class FeeData(
    val gasPrice: String,
    val level: String = "Medium",
) {
    val fee: String = when (level) {
        "Medium" -> computeFee(72238U)
        "High" -> computeFee(97888U)
        else -> computeFee(21000U)
    }

    private fun computeFee(gasUnits: UInt): String = let {
        gasPrice.toULongOrNull()?.let {
            it * gasUnits
        } ?: 0
    }.toString()
}
