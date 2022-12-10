package id.walt.gateway.providers.metaco.restapi.intent.model.dryrun.estimate

import kotlinx.serialization.Serializable

@Serializable
data class BitcoinEstimate(
    val vbytes: String? = null
) : Estimate("Bitcoin")