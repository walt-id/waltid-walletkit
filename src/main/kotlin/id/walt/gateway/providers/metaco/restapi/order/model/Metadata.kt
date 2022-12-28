package id.walt.gateway.providers.metaco.restapi.order.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.models.customproperties.CustomProperties
import kotlinx.serialization.Serializable

@Serializable
data class Metadata(
    val createdAt: String,
    val createdBy: CreatedBy?,
    val customProperties: CustomProperties,
    @Json(serializeNull = false)
    val description: String?,
    val lastModifiedAt: String,
    @Json(serializeNull = false)
    val lastModifiedBy: LastModifiedBy?,
    val revision: Int
)