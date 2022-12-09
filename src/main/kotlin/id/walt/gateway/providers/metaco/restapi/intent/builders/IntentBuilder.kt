package id.walt.gateway.providers.metaco.restapi.intent.builders

import id.walt.gateway.dto.trades.TradePreview
import id.walt.gateway.providers.metaco.restapi.intent.model.intent.NoSignatureIntent

interface IntentBuilder {
    fun build(params: TradePreview): NoSignatureIntent

    companion object {
        fun getBuilder(type: String): IntentBuilder = when (type) {
            "Ethereum" -> EthereumIntentBuilder()
            else -> throw IllegalArgumentException("No builder for intent type.")
        }
    }
}