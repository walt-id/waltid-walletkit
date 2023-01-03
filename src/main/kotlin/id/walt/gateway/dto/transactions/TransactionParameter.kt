package id.walt.gateway.dto.transactions

import kotlinx.serialization.Serializable

@Serializable
data class TransactionParameter(
    val domainId: String,
    val transactionId: String,
    val criteria: Map<String, String> = emptyMap(),
)
