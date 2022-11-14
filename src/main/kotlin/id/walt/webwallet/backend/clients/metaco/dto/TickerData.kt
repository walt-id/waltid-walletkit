package id.walt.webwallet.backend.clients.metaco.dto

data class TickerData(
    val kind: String,
    val symbol: String,
    val balance: String,
    val value: ValueWithChange,
    val priceChange: String,
    val marketCap: String,
    val allTimeHigh: String,
    val allTimeLow: String,
)
