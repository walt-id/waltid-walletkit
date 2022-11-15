package id.walt.gateway.dto

data class AccountParameter(
    val domainId: String,
    val accountId: String,
    val criteria: Map<String, String> = emptyMap(),
)
