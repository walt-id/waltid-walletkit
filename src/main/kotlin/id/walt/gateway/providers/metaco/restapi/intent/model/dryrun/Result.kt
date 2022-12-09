package id.walt.gateway.providers.metaco.restapi.intent.model.dryrun

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class Result(
    @Json(serializeNull = false)
    val hint: String? = null,
    @Json(serializeNull = false)
    val reason: String? = null,
    val type: String
)