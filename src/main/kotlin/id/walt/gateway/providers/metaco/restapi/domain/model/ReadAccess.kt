package id.walt.gateway.providers.metaco.restapi.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ReadAccess(
    val domains: List<String>,
    val users: List<String>,
    val endpoints: List<String>,
    val policies: List<String>,
    val accounts: List<String>,
    val transactions: List<String>,
    val requests: List<String>,
    val events: List<String>,
)
