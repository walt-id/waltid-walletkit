package id.walt.gateway.providers.metaco.restapi.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Domain(
    val data: Data,
    val signature: String,
)
