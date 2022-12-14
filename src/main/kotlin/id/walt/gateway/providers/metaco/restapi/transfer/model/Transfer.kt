package id.walt.gateway.providers.metaco.restapi.transfer.model

import com.beust.klaxon.Json
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
    val recipient: TransferParty?,
    @Json(serializeNull = false)
    val registeredAt: String?,
    @Json(serializeNull = false)
    val sender: TransferParty?,
    val senders: List<TransferParty>,
    val tickerId: String,
    val transactionId: String,
    val value: String
)