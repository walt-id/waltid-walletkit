package id.walt.gateway.providers.metaco.restapi.intent.builders

import id.walt.gateway.dto.intents.IntentBuilderParam
import id.walt.gateway.dto.intents.IntentData
import id.walt.gateway.dto.intents.IntentParameter
import id.walt.gateway.providers.metaco.restapi.intent.model.Intent

interface IntentBuilder<T: IntentData> {
    fun build(parameter: IntentParameter<T>): Intent

    companion object {
        //TODO: fix return type
        fun getBuilder(param: IntentBuilderParam): Any = when (param.payloadType) {
            "v0_CreateTransactionOrder" -> TransactionOrderIntentBuilder(param.parameterType)
            "v0_CreateTransferOrder" -> TransferOrderIntentBuilder()
            "v0_ValidateTickers" -> ValidateTickersIntentBuilder()
            else -> throw IllegalArgumentException("No builder for type ${param.payloadType}")
        }
    }
}