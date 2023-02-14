package id.walt.gateway.providers.metaco.restapi.transaction.model.ledgerdata

import id.walt.gateway.providers.metaco.restapi.transaction.model.Input
import id.walt.gateway.providers.metaco.restapi.transaction.model.Output
import kotlinx.serialization.Serializable

@Serializable
class BitcoinLedgerData(
    val inputs: List<Input>,
    val outputs: List<Output>,
) : LedgerData("Bitcoin")
