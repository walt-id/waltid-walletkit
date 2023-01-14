package id.walt.gateway.providers.metaco.restapi.intent.builders

import id.walt.gateway.dto.trades.TransferParameter
import id.walt.gateway.providers.metaco.restapi.models.parameters.Parameters

interface ParameterBuilder {
    fun build(params: TransferParameter): Parameters

    companion object {
        fun getBuilder(type: String): ParameterBuilder = when (type) {
            "Ethereum" -> EthereumParamBuilder()
            "Bitcoin" -> BitcoinParamBuilder()
            else -> throw IllegalArgumentException("No builder for type $type")
        }
    }
}