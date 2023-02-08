package id.walt.gateway.dto.accounts

import kotlinx.serialization.Serializable

@Serializable
data class AccountBasicData(
    val domainName: String,
    val accountAlias: String,
    val address: List<String>,
)
