package id.walt.gateway.providers.metaco.restapi.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Permissions(
    val readAccess: ReadAccess,
)
