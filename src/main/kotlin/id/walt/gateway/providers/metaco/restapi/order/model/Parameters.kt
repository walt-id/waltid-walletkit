package id.walt.gateway.providers.metaco.restapi.order.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.order.model.Destination
import id.walt.gateway.providers.metaco.restapi.order.model.FeeStrategy
import kotlinx.serialization.Serializable

@Serializable
data class Parameters(
    val amount: String,
    @Json(serializeNull = false)
    val data: String?,
    val destination: Destination,
    val feeStrategy: FeeStrategy,
    val maximumFee: String,
    @Json(serializeNull = false)
    val resourceStrategy: String?,
    val type: String
)