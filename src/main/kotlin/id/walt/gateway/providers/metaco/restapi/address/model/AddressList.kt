package id.walt.gateway.providers.metaco.restapi.address.model

import kotlinx.serialization.Serializable

@Serializable
data class AddressList(
    val items: List<Address>,
    val count: Int,
    val currentStartingAfter: String? = null,
    val nextStartingAfter: String? = null
)