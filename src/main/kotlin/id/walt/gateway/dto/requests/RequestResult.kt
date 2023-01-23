package id.walt.gateway.dto.requests

import kotlinx.serialization.Serializable

@Serializable
data class RequestResult(
    val result: Boolean,
    val message: String? = null,
)
