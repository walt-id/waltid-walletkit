package id.walt.gateway.providers.metaco.restapi.services

import kotlinx.serialization.Serializable

@Serializable
data class SignChallengeResponse(
    val canonicalPayload: String,
    val hash: String,
    val signature: String,
)
