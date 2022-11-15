package id.walt.gateway.providers.metaco.restapi.transfer.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.transfer.model.AddressDetails
import kotlinx.serialization.Serializable

@Serializable
data class Recipient(
    val accountId: String,
    @Json(serializeNull = false)
    val address: String?,
    @Json(serializeNull = false)
    val addressDetails: AddressDetails?,
    @Json(serializeNull = false)
    val domainId: String?,
    val type: String
)