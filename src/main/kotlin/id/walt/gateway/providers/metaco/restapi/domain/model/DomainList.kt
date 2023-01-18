package id.walt.gateway.providers.metaco.restapi.domain.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class DomainList(
    val items: List<Domain>,
    val count: Int,
    @Json(serializeNull = false)
    val currentStartingAfter: String? = null,
    @Json(serializeNull = false)
    val nextStartingAfter: String? = null,
)
