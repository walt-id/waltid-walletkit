package id.walt.gateway.providers.metaco.restapi.models

import kotlinx.serialization.Serializable

@Serializable
abstract class EntityList<T> {
    abstract val items: List<T>
    abstract val count: Int
    abstract val currentStartingAfter: String?
    abstract val nextStartingAfter: String?
}