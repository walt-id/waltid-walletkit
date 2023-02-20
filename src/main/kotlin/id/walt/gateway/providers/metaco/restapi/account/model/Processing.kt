package id.walt.gateway.providers.metaco.restapi.account.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class Processing(
    @Json(serializeNull = false)
    val hint: String? = null,
    val status: String,
)