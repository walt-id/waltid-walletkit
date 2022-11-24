package id.walt.gateway.providers.metaco.restapi.transaction.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.transaction.model.AccountReference
import kotlinx.serialization.Serializable

@Serializable
data class Input(
    val accountReference: AccountReference,
    val address: String,
    val index: Int,
    @Json(serializeNull = false)
    val scriptPubKey: String?,
    @Json(serializeNull = false)
    val transactionHash: String?
)