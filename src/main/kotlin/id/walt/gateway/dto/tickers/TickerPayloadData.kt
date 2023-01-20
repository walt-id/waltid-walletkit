package id.walt.gateway.dto.tickers

import id.walt.gateway.dto.intents.PayloadData
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.ValidateTickersPayload

data class TickerPayloadData(
    val ticker: ValidateTickersPayload.TickerData,
) : PayloadData
