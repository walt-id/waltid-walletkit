package id.walt.gateway.providers.metaco.restapi.account.model

import kotlinx.serialization.Serializable

@Serializable
data class ProviderDetails(
    val keyInformation: KeyInformation,
    val keyStrategy: String,
    val type: String,
    val vaultId: String
)