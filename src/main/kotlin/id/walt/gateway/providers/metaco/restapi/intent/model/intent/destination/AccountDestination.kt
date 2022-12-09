package id.walt.gateway.providers.metaco.restapi.intent.model.intent.destination

import kotlinx.serialization.Serializable

@Serializable
data class AccountDestination(
    val accountId: String,
) : Destination() {
    override val type = "Account"
}