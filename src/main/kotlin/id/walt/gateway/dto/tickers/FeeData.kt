package id.walt.gateway.dto.tickers

import kotlinx.serialization.Serializable

@Serializable
data class FeeData(
    val gasPrice: String,
    val level: String = "Medium",
) {
    val fee: String = when (level.lowercase()) {
        "high" -> computeFee(97888U)
        "medium" -> computeFee(72238U)
        else -> computeFee(21000U)
    }

    private fun computeFee(gasUnits: UInt): String = let {
        gasPrice.toULongOrNull()?.let {
            (1.1 * (it * gasUnits).toDouble()).toULong()
        } ?: 0
    }.toString()
}