package id.walt.gateway.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProfileData(
    val profileId: String,
    val accounts: List<AccountData>,
)