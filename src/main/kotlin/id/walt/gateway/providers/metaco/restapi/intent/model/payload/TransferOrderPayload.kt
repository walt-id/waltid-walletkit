package id.walt.gateway.providers.metaco.restapi.intent.model.payload

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.models.customproperties.CustomProperties
import id.walt.gateway.providers.metaco.restapi.models.parameters.Output
import kotlinx.serialization.Serializable

@Serializable
class TransferOrderPayload(
    val id: String,
    val accountId: String,
    val tickerId: String,
    val outputs: List<Output>,
    val feeStrategy: String,
    val maximumFee: String,
    @Json(serializeNull = false)
    val description: String? = null,
    val customProperties: CustomProperties,
) : Payload(Types.CreateTransferOrder.value)
