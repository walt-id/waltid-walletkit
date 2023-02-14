package id.walt.gateway.dto.balances

import id.walt.gateway.Common
import id.walt.gateway.dto.ValueWithChange
import id.walt.gateway.dto.tickers.TickerData
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
