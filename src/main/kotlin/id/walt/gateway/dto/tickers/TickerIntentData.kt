package id.walt.gateway.dto.tickers

import id.walt.gateway.dto.intents.IntentData
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.ValidateTickersPayload

data class TickerIntentData(
    val targetDomainId: String,
    val ticker: ValidateTickersPayload.TickerData,
) : IntentData
