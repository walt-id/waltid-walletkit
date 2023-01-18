package id.walt.gateway.providers.metaco.restapi.order.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val data: Data,
    @Json(serializeNull = false)
    val signature: String?
)