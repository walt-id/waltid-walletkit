package id.walt.webwallet.backend.clients.metaco.dto

import kotlinx.serialization.Serializable

@Serializable
data class ValueWithChange(
    val value: String,
    val change: String,
)
