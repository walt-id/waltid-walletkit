package id.walt.gateway.providers.metaco.restapi.models.customproperties

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class CustomPropertiesModel(
    @Json(serializeNull = false)
    val transactionType: String? = null,
)