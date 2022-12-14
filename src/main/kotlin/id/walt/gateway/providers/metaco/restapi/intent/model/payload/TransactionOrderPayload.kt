package id.walt.gateway.providers.metaco.restapi.intent.model.payload

import id.walt.gateway.providers.metaco.restapi.intent.model.parameters.Parameters
import id.walt.gateway.providers.metaco.restapi.intent.model.CustomProperties
import kotlinx.serialization.Serializable

@Serializable
class TransactionOrderPayload(
    val id: String,
    val accountId: String,
    val parameters: Parameters,
    val description: String? = null,
    val customProperties: CustomProperties,
) : Payload("v0_CreateTransactionOrder")