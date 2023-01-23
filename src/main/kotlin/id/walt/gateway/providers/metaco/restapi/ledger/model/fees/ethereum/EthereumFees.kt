package id.walt.gateway.providers.metaco.restapi.ledger.model.fees.ethereum

import id.walt.gateway.providers.metaco.restapi.ledger.model.fees.Fees
import kotlinx.serialization.Serializable

@Serializable
data class EthereumFees(
    val high: LevelFee,
    val medium: LevelFee,
    val low: LevelFee,
) : Fees("Ethereum")
