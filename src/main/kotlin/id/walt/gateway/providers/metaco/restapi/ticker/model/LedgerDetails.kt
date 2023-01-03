package id.walt.gateway.providers.metaco.restapi.ticker.model

import id.walt.gateway.providers.metaco.restapi.ticker.model.ledgerproperties.LedgerProperties
import kotlinx.serialization.Serializable

@Serializable
data class LedgerDetails(
    val properties: LedgerProperties,
    val type: String
)