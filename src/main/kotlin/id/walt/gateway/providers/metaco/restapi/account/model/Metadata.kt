package id.walt.gateway.providers.metaco.restapi.account.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class Metadata(
    @Json(serializeNull = false)
    val createdAt: String?,
    @Json(serializeNull = false)
    val createdBy: CreatedBy?,
    @Json(serializeNull = false)
    val customProperties: CustomProperties?,
    @Json(serializeNull = false)
    val description: String?,
    @Json(serializeNull = false)
    val lastModifiedAt: String?,
    @Json(serializeNull = false)
    val lastModifiedBy: LastModifiedBy?,
    @Json(serializeNull = false)
    val revision: Int?
)