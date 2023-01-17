package id.walt.gateway.dto.accounts

import kotlinx.serialization.Serializable

@Serializable
data class AccountParameter(
    val accountIdentifier: AccountIdentifier,
    val criteria: Map<String, String> = emptyMap(),
)
