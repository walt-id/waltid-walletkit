package id.walt.gateway.providers.metaco.restapi.account.model

import kotlinx.serialization.Serializable

@Serializable
data class PublicKey(
    val chainCode: String,
    val type: String,
    val value: String
)