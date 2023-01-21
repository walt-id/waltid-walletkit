package id.walt.gateway.providers.metaco.restapi.models

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
abstract class EntityList<T> {
    abstract val items: List<T>
    abstract val count: Int
    @Json(serializeNull = false)
    abstract val currentStartingAfter: String?
    @Json(serializeNull = false)
    abstract val nextStartingAfter: String?
}