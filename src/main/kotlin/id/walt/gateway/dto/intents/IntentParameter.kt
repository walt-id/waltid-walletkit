package id.walt.gateway.dto.intents

import id.walt.gateway.dto.users.UserIdentifier

data class IntentParameter(
    val targetDomainId: String,
    val author: UserIdentifier,
    val type: String? = null,
    val expiry: String,
)
