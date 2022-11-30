package id.walt.gateway.providers.metaco.restapi.account.model

import kotlinx.serialization.Serializable

@Serializable
data class KeyInformation(
    val derivationPath: String,
    val publicKey: PublicKey,
    val type: String
)