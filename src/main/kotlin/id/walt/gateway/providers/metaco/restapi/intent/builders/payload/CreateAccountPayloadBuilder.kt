package id.walt.gateway.providers.metaco.restapi.intent.builders.payload

import id.walt.gateway.dto.CreateAccountPayloadData
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.CreateAccountPayload
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import java.util.*

class CreateAccountPayloadBuilder : PayloadBuilder<CreateAccountPayloadData> {
    override fun build(data: CreateAccountPayloadData): Payload = CreateAccountPayload(
        id = UUID.randomUUID().toString(),
        alias = data.alias,
        ledgerId = data.ledgerId,
        lock = data.lock,
        providerDetails = CreateAccountPayload.CreateAccountProviderDetailsPayload(
            vaultId = data.vaultId,
            keyStrategy = data.keyStrategy,
        )
    )
}