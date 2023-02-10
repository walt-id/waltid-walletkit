package id.walt.gateway.providers.metaco.restapi.intent.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val author: Author,
    val expiryAt: String,
    val targetDomainId: String,
    val id: String,
    val payload: Payload,
    val description: String? = null,
    val customProperties: Map<String, String>,
    @Json(serializeNull = false)
    val type: String? = null,
)