package id.walt.gateway.providers.metaco.restapi.intent.model.payload

import id.walt.gateway.providers.metaco.restapi.models.customproperties.CustomPropertiesModel
import id.walt.gateway.providers.metaco.restapi.models.parameters.Parameters
import kotlinx.serialization.Serializable

@Serializable
class TransactionOrderPayload(
    val id: String,
    val accountId: String,
    val parameters: Parameters,
    val description: String? = null,
    val customProperties: CustomPropertiesModel,
) : Payload(Types.CreateTransactionOrder.value)