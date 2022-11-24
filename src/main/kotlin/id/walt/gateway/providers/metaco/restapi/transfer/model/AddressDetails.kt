package id.walt.gateway.providers.metaco.restapi.transfer.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class AddressDetails(
    val address: String,
    val resolvedEndpoints: List<String>
)