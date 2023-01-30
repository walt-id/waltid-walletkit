package id.walt.gateway.providers.metaco.restapi.models.metadata

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.models.customproperties.CustomPropertiesModel
import kotlinx.serialization.Serializable

@Serializable
data class Metadata(
    val createdAt: String,
    @Json(serializeNull = false)
    val createdBy: CreatedBy? = null,
    val customProperties: CustomPropertiesModel,
    @Json(serializeNull = false)
    val description: String? = null,
    val lastModifiedAt: String,
    @Json(serializeNull = false)
    val lastModifiedBy: LastModifiedBy? = null,
    val revision: Int,
)