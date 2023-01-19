package id.walt.gateway.dto.intents

import id.walt.gateway.dto.users.UserIdentifier
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

data class IntentParameter<T>(
    val data: T,
    val author: UserIdentifier,
    val type: String = "Propose",
    val expiry: Instant = Instant.now().plus(Duration.ofHours(1)).truncatedTo(ChronoUnit.SECONDS),
)
