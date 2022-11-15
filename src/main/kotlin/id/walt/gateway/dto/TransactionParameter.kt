package id.walt.gateway.dto

data class TransactionParameter(
    val domainId: String,
    val transactionId: String,
    val criteria: Map<String, String> = emptyMap(),
)
