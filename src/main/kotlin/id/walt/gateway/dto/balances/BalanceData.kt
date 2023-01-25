package id.walt.gateway.dto.balances

import id.walt.gateway.Common
import id.walt.gateway.dto.tickers.TickerData
import id.walt.gateway.dto.ValueWithChange
import kotlinx.serialization.Serializable

@Serializable
data class BalanceData(
    val amount: String,
    val ticker: TickerData,
) {
    val price: ValueWithChange = ValueWithChange(
        Common.computeAmount(amount, ticker.decimals) * ticker.askPrice.value,
        Common.computeAmount(amount, ticker.decimals) * ticker.askPrice.change,
        ticker.askPrice.currency
    )

}
