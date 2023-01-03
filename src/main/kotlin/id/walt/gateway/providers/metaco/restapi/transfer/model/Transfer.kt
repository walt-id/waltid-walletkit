package id.walt.gateway.providers.metaco.restapi.transfer.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.transfer.model.metadata.TransferMetadata
import id.walt.gateway.providers.metaco.restapi.transfer.model.transferparty.TransferParty
import kotlinx.serialization.Serializable

@Serializable
data class Transfer(
    val id: String,
    @Json(serializeNull = false)
    val transactionId: String? = null,
    val tickerId: String,
    val quarantined: Boolean,
    @Json(serializeNull = false)
    val sender: TransferParty? = null,
    val senders: List<TransferParty>,
    @Json(serializeNull = false)
    val recipient: TransferParty? = null,
    val value: String,
    val kind: String,
    val metadata: TransferMetadata,
    val registeredAt: String,
)