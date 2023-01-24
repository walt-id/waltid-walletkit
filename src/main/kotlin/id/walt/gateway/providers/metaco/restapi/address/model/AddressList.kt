package id.walt.gateway.providers.metaco.restapi.address.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.models.EntityList
import kotlinx.serialization.Serializable

@Serializable
data class AddressList(
    override val items: List<Address>,
    override val count: Int,
    @Json(serializeNull = false)
    override val currentStartingAfter: String?,
    @Json(serializeNull = false)
    override val nextStartingAfter: String?
) : EntityList<Address>()