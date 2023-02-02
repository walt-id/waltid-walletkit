package id.walt.gateway.dto.transactions

import kotlinx.serialization.Serializable

@Serializable
data class TransactionParameter(
    val domainId: String,
    val accountId: String,
    val transactionId: String,
)
