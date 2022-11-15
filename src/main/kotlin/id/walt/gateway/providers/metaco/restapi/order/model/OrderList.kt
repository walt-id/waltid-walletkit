package id.walt.gateway.providers.metaco.restapi.order.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.order.model.Order
import kotlinx.serialization.Serializable

@Serializable
data class OrderList(
    val items: List<Order>,
    val count: Int,
    @Json(serializeNull = false)
    val currentStartingAfter: String?,
    @Json(serializeNull = false)
    val nextStartingAfter: String?
)