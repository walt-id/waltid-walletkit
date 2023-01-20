package id.walt.gateway.dto.accounts

import kotlinx.serialization.Serializable

@Serializable
data class AccountIdentifier(
    val domainId: String,
    val accountId: String,
) {
    fun isEmpty() = domainId.isEmpty() && accountId.isEmpty()
}
