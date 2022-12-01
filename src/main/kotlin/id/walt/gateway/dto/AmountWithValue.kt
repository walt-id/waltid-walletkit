package id.walt.gateway.dto

import id.walt.gateway.Common
import kotlinx.serialization.Serializable

@Serializable
data class AmountWithValue(
    val amount: String,
    val ticker: TickerData,
) {
    val value: Double = Common.computeAmount(amount, ticker.decimals) * ticker.price.value
}
