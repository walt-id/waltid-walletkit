package id.walt.gateway.dto

import id.walt.gateway.Common
import kotlinx.serialization.Serializable

@Serializable
data class BalanceData(
    val amount: String,
    val ticker: TickerData,
) {
    val price: ValueWithChange = ValueWithChange(
        Common.computeAmount(amount, ticker.decimals) * ticker.price.value,
        Common.computeAmount(amount, ticker.decimals) * ticker.price.change,
        ticker.price.currency
    )

}
