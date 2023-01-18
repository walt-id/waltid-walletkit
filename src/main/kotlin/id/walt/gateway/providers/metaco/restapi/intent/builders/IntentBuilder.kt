package id.walt.gateway.providers.metaco.restapi.intent.builders

import id.walt.gateway.dto.intents.IntentBuilderParam
import id.walt.gateway.dto.intents.IntentParameter
import id.walt.gateway.providers.metaco.restapi.intent.model.Intent

interface IntentBuilder {
    fun build(parameter: IntentParameter): Intent

    companion object {
        fun getBuilder(param: IntentBuilderParam): IntentBuilder = when (param.payloadType) {
            "v0_CreateTransactionOrder" -> TransactionOrderIntentBuilder(param.parameterType)
            else -> throw IllegalArgumentException("No builder for type ${param.payloadType}")
        }
    }
}