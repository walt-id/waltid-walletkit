package id.walt.gateway.providers.metaco.restapi.order.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class CustomProperties(
    @Json(serializeNull = false)
    val enhancedDestinationReference: String?
)