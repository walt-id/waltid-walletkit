package id.walt.gateway.providers.metaco.restapi.ledger.model

import kotlinx.serialization.Serializable

@Serializable
data class Ledger(
    val data: Data,
    val signature: String,
)
