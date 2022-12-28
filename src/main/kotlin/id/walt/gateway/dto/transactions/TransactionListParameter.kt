package id.walt.gateway.dto.transactions

import kotlinx.serialization.Serializable

@Serializable
data class TransactionListParameter(
    val domainId: String,
    val accountId: String,
    val tickerId: String? = null,
)
