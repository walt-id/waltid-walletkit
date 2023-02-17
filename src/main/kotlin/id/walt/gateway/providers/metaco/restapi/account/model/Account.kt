package id.walt.gateway.providers.metaco.restapi.account.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class Account(
    @Json(serializeNull = false)
    val additionalDetails: AdditionalDetails? = null,
    val data: Data,
    val signature: String
)