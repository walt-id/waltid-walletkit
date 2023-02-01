package id.walt.gateway.providers.metaco.restapi.intent.model.payload

import kotlinx.serialization.Serializable

@Serializable
data class ReleaseQuarantinedTransfersPayload(
    val accountId: String,
    val transferIds: List<String>,
) : Payload(Types.ReleaseQuarantinedTransfers.value)
