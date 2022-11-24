package id.walt.gateway.dto

import kotlinx.serialization.Serializable

@Serializable
data class BalanceData(
    val amount: String,
    val ticker: TickerData,
    val price: ValueWithChange = ValueWithChange()
)
