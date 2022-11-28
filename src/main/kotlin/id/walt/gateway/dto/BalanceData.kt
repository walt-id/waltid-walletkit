package id.walt.gateway.dto

import kotlinx.serialization.Serializable

@Serializable
data class BalanceData(
    val amount: String,
    val ticker: TickerData,
) {
    val price: ValueWithChange = amount.toDoubleOrNull()?.let {
        ValueWithChange(it * ticker.price.value, it * ticker.price.change)
    } ?: ValueWithChange()

}
