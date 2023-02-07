package id.walt.gateway.dto.accounts

import kotlinx.serialization.Serializable

@Serializable
data class AccountInitiationParameter(
    val domainName: String,
    val accountName: String,
    val ledgerId: String,
)
