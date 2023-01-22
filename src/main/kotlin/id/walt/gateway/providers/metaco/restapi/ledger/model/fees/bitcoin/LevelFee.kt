package id.walt.gateway.providers.metaco.restapi.ledger.model.fees.bitcoin

import kotlinx.serialization.Serializable

@Serializable
data class LevelFee(
    val satoshiPerVbyte: String,
)
