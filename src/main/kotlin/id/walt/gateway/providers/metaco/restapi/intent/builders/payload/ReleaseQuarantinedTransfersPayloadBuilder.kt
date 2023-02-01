package id.walt.gateway.providers.metaco.restapi.intent.builders.payload

import id.walt.gateway.dto.QuarantineTransferPayloadData
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.ReleaseQuarantinedTransfersPayload

class ReleaseQuarantinedTransfersPayloadBuilder : PayloadBuilder<QuarantineTransferPayloadData> {
    override fun build(data: QuarantineTransferPayloadData): Payload = ReleaseQuarantinedTransfersPayload(
        accountId = data.accountId,
        transferIds = data.transfers,
    )
}