package id.walt.gateway.providers.metaco.restapi.order.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.order.model.CreatedBy
import id.walt.gateway.providers.metaco.restapi.order.model.CustomProperties
import id.walt.gateway.providers.metaco.restapi.order.model.LastModifiedBy
import kotlinx.serialization.Serializable

@Serializable
data class Metadata(
    val createdAt: String,
    val createdBy: CreatedBy?,
    @Json(serializeNull = false)
    val customProperties: CustomProperties?,
    @Json(serializeNull = false)
    val description: String?,
    @Json(serializeNull = false)
    val lastModifiedAt: String?,
    @Json(serializeNull = false)
    val lastModifiedBy: LastModifiedBy?,
    val revision: Int
)