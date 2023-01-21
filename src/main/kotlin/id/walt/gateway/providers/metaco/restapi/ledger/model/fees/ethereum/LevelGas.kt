package id.walt.gateway.providers.metaco.restapi.ledger.model.fees.ethereum

import kotlinx.serialization.Serializable

@Serializable
data class LevelGas(
    val gasPrice: String,
)
