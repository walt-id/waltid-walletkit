package id.walt.gateway.dto.profiles

import id.walt.gateway.dto.accounts.AccountData
import kotlinx.serialization.Serializable

@Serializable
data class ProfileData(
    val profileId: String,
    val accounts: List<AccountData>,
)