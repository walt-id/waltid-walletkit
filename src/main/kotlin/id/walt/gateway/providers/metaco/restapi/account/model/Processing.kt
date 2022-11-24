package id.walt.gateway.providers.metaco.restapi.account.model

import kotlinx.serialization.Serializable

@Serializable
data class Processing(
    val status: String
)