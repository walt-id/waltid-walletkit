package id.walt.gateway.dto.transactions

import kotlinx.serialization.Serializable

@Serializable
data class TransactionData(
    val id: String,
    val relatedAccount: String,
    val amount: String,
    val meta: Map<String, String>,
    val date: String,
    val status: String,
)
