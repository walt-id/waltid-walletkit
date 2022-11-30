package id.walt.gateway.dto

import kotlinx.serialization.Serializable

@Serializable
data class AccountParameter(
    val domainId: String,
    val accountId: String,
    val criteria: Map<String, String> = emptyMap(),
)
