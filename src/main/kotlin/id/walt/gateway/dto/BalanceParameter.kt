package id.walt.gateway.dto

data class BalanceParameter(
    val domainId: String,
    val accountId: String,
    val tickerId: String,
    val criteria: Map<String, String> = emptyMap(),
)
