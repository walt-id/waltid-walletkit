package id.walt.gateway.providers.metaco.restapi.transaction.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class Input(
    @Json(serializeNull = false)
    val accountReference: AccountReference? = null,
    @Json(serializeNull = false)
    val address: String? = null,
    val index: Int,
    @Json(serializeNull = false)
    val scriptPubKey: String? = null,
    @Json(serializeNull = false)
    val transactionHash: String? = null,
)