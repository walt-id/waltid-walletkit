package id.walt.gateway.providers.metaco.restapi.intent.builders.payload

import id.walt.gateway.dto.tickers.TickerPayloadData
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.ValidateTickersPayload

class ValidateTickersPayloadBuilder : PayloadBuilder<TickerPayloadData> {
    override fun build(data: TickerPayloadData): Payload = ValidateTickersPayload(
        listOf(data.ticker)
    )
}