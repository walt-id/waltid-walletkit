package id.walt.gateway.providers.metaco.restapi.transfer.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.transfer.model.Transfer
import kotlinx.serialization.Serializable

@Serializable
data class TransferList(
    val items: List<Transfer>,
    val count: Int,
    @Json(serializeNull = false)
    val currentStartingAfter: String?,
    @Json(serializeNull = false)
    val nextStartingAfter: String?
)
