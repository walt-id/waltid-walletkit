package id.walt.gateway.providers.metaco.restapi.transaction.model.ledgerdata

import id.walt.gateway.providers.metaco.restapi.transaction.model.LedgerDataLog
import kotlinx.serialization.Serializable

@Serializable
class EthereumLedgerData(
    val logs: List<LedgerDataLog>,
) : LedgerData("Ethereum") {
}