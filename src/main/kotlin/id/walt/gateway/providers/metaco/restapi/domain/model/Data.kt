package id.walt.gateway.providers.metaco.restapi.domain.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.models.metadata.Metadata
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    val id: String,
    @Json(serializeNull = false)
    val parentId: String? = null,
    val alias: String,
    val lock: String,//TODO: define enum class
    val governingStrategy: String,//TODO: define enum class
    val permissions: Permissions,
    val metadata: Metadata,
)
