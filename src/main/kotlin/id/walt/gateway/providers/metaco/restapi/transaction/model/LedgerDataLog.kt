package id.walt.gateway.providers.metaco.restapi.transaction.model

import kotlinx.serialization.Serializable

@Serializable
data class LedgerDataLog(
    val logIndex: String,
    val address: String,
    val data: String,
    val topics: List<String>,
)