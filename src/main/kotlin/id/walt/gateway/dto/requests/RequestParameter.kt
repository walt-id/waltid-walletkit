package id.walt.gateway.dto.requests

import id.walt.gateway.dto.intents.PayloadData
import kotlinx.serialization.Serializable

@Serializable
data class RequestParameter(
    val payloadType: String,
    val targetDomainId: String,
    val data: PayloadData,
    val ledgerType: String? = null,
)
