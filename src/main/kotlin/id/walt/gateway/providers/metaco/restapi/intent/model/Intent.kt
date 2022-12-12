package id.walt.gateway.providers.metaco.restapi.intent.model

import kotlinx.serialization.Serializable

interface Intent {
    val request: Request
}

@Serializable
data class NoSignatureIntent(
    override val request: Request,
) : Intent

data class SignatureIntent(
    override val request: Request,
    val signature: String,
) : Intent