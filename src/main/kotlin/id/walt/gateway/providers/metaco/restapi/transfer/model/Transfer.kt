package id.walt.gateway.providers.metaco.restapi.transfer.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.transfer.model.Metadata
import id.walt.gateway.providers.metaco.restapi.transfer.model.Recipient
import id.walt.gateway.providers.metaco.restapi.transfer.model.Sender
import kotlinx.serialization.Serializable

@Serializable
data class Transfer(
    val id: String,
    val kind: String,
    @Json(serializeNull = false)
    val metadata: Metadata?,
    @Json(serializeNull = false)
    val quarantined: Boolean?,
    @Json(serializeNull = false)
    val recipient: Recipient?,
    @Json(serializeNull = false)
    val registeredAt: String?,
    @Json(serializeNull = false)
    val sender: Sender?,
    val senders: List<Sender>,
    val tickerId: String,
    val transactionId: String,
    val value: String
)