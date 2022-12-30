package id.walt.gateway.dto.transactions

import id.walt.gateway.dto.TickerData
import id.walt.gateway.dto.ValueWithChange
import kotlinx.serialization.Serializable

@Serializable
data class TransactionData(
    val id: String,
    val relatedAccount: String,
    val amount: String,
    val ticker: TickerData,
    val price: ValueWithChange,
    val date: String,
    val type: String,
    val status: String,
)
