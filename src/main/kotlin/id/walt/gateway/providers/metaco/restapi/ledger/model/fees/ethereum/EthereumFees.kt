package id.walt.gateway.providers.metaco.restapi.ledger.model.fees.ethereum

import id.walt.gateway.providers.metaco.restapi.ledger.model.fees.Fees
import kotlinx.serialization.Serializable

@Serializable
data class EthereumFees(
    override val high: EthereumPriorityStrategy,
    override val medium: EthereumPriorityStrategy,
    override val low: EthereumPriorityStrategy,
) : Fees("Ethereum")
