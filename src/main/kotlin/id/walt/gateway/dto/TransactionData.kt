package id.walt.gateway.dto

import id.walt.gateway.metaco.dto.TickerData
import id.walt.gateway.metaco.dto.ValueWithChange

data class TransactionData(
    val id: String,
    val sender: String? = null,
    val recipient: String? = null,
    val amount: String,
    val ticker: TickerData,
    val price: ValueWithChange = ValueWithChange("",""),
    val status: String = "unknown",
)
