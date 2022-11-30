package id.walt.gateway.providers.metaco.restapi.transfer.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class AddressesDetail(
    @Json(serializeNull = false)
    val address: String?,
    @Json(serializeNull = false)
    val resolvedEndpoints: List<String>?
)