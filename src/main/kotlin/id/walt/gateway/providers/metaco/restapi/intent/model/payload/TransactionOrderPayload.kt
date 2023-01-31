package id.walt.gateway.providers.metaco.restapi.intent.model.payload

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.models.parameters.Parameters
import kotlinx.serialization.Serializable

@Serializable
data class TransactionOrderPayload(
    val id: String,
    val accountId: String,
    val parameters: Parameters,
    @Json(serializeNull = false)
    val description: String? = null,
    val customProperties: Map<String, String>,
) : Payload(Types.CreateTransactionOrder.value)