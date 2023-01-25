package id.walt.gateway.providers.metaco.restapi.domain.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.models.EntityList
import kotlinx.serialization.Serializable

@Serializable
data class DomainList(
    override val items: List<Domain>,
    override val count: Int,
    @Json(serializeNull = false)
    override val currentStartingAfter: String?,
    @Json(serializeNull = false)
    override val nextStartingAfter: String?
) : EntityList<Domain>()