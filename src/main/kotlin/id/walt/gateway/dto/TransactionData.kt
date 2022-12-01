package id.walt.gateway.dto

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class TransactionData(
    val id: String,
    val relatedAccount: String = "unknown",
    val amount: String,
    val ticker: TickerData,
    val price: ValueWithChange = ValueWithChange(),
    val date: String = LocalDateTime.now().toString(),
    val type: String,
    val status: String = "unknown",
)
