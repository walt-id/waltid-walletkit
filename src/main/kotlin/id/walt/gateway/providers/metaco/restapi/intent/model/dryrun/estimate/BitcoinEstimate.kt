package id.walt.gateway.providers.metaco.restapi.intent.model.dryrun.estimate

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class BitcoinEstimate(
    @Json(serializeNull = false)
    override val hint: String? = null,
    @Json(serializeNull = false)
    override val reason: String? = null,
    override val type: String,
    @Json(serializeNull = false)
    val vbytes: String? = null
) : Estimate()