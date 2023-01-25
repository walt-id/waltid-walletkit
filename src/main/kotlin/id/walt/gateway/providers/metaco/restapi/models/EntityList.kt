package id.walt.gateway.providers.metaco.restapi.models

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class EntityList<T>(
    val items: List<T>,
    val count: Int,
    @Json(serializeNull = false)
    val currentStartingAfter: String? = null,
    @Json(serializeNull = false)
    val nextStartingAfter: String? = null,
)