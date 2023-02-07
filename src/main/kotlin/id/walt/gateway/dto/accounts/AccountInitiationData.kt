package id.walt.gateway.dto.accounts

import kotlinx.serialization.Serializable

@Serializable
data class AccountInitiationData(
    val domainName: String,
    val accountValue: String,
    val address: String?,
)
