package id.walt.gateway.providers.metaco.restapi.intent.model.estimate

import kotlinx.serialization.Serializable

@Serializable
data class BitcoinEstimate(
    val vbytes: String? = null
) : Estimate("Bitcoin")