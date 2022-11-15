package id.walt.gateway.dto

data class BalanceData(
    val amount: String,
    val ticker: TickerData,
    val price: ValueWithChange = ValueWithChange("", "")
)
