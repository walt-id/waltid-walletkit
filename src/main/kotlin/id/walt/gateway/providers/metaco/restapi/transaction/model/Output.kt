package id.walt.gateway.providers.metaco.restapi.transaction.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class Output(
    @Json(serializeNull = false)
    val accountReference: AccountReference? = null,
    @Json(serializeNull = false)
    val address: String? = null,
    val amount: String,
    val index: Int,
    @Json(serializeNull = false)
    val scriptPubKey: String? = null,
)