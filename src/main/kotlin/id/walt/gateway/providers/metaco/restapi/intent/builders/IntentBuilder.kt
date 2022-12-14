package id.walt.gateway.providers.metaco.restapi.intent.builders

import id.walt.gateway.dto.IntentBuilderParam
import id.walt.gateway.dto.trades.TradeParameter
import id.walt.gateway.providers.metaco.restapi.intent.model.Intent

interface IntentBuilder {
    fun build(params: TradeParameter): Intent

    companion object {
        fun getBuilder(param: IntentBuilderParam): IntentBuilder = when (param.payloadType) {
            "v0_CreateTransactionOrder" -> TransactionOrderIntentBuilder(param.parameterType)
            else -> throw IllegalArgumentException("No builder for type ${param.payloadType}")
        }
    }
}