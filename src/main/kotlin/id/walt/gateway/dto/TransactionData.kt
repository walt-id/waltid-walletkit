package id.walt.gateway.dto

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

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
