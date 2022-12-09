package id.walt.gateway.providers.metaco.restapi.intent.model.dryrun.estimate

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class EthereumEstimate(
//    @Json(serializeNull = false)
    override val hint: String? = null,
//    @Json(serializeNull = false)
    override val reason: String? = null,
    override val type: String,
    @Json(serializeNull = false)
    val defaultedToBlockGasLimit: String? = null,
    @Json(serializeNull = false)
    val gas: String? = null,
) : Estimate()