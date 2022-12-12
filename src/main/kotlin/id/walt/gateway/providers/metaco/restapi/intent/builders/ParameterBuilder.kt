package id.walt.gateway.providers.metaco.restapi.intent.builders

import id.walt.gateway.dto.trades.TradePreview
import id.walt.gateway.providers.metaco.restapi.intent.model.parameters.Parameters

interface ParameterBuilder {
    fun build(params: TradePreview): Parameters

    companion object {
        fun getBuilder(type: String): ParameterBuilder = when (type) {
            "Ethereum" -> EthereumParamBuilder()
            "Bitcoin" -> BitcoinParamBuilder()
            else -> throw IllegalArgumentException("No builder for type $type")
        }
    }
}