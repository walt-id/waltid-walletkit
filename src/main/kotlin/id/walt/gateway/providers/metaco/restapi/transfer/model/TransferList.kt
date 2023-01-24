package id.walt.gateway.providers.metaco.restapi.transfer.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.models.EntityList
import kotlinx.serialization.Serializable

@Serializable
data class TransferList(
    override val items: List<Transfer>,
    override val count: Int,
    @Json(serializeNull = false)
    override val currentStartingAfter: String?,
    @Json(serializeNull = false)
    override val nextStartingAfter: String?
) : EntityList<Transfer>()