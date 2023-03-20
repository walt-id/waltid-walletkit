package id.walt.webwallet.backend.quick.setup

import kotlinx.serialization.Serializable

@Serializable
data class QuickSetupRequest(
    val hosts: List<String>,
)
