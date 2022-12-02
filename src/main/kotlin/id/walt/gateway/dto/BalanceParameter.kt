package id.walt.gateway.dto

import kotlinx.serialization.Serializable

@Serializable
data class BalanceParameter(
    val domainId: String,
    val accountId: String,
    val tickerId: String,
    val criteria: Map<String, String> = emptyMap(),
)