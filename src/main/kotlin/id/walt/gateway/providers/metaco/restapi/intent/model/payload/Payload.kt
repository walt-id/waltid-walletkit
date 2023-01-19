package id.walt.gateway.providers.metaco.restapi.intent.model.payload

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
@TypeFor(field = "type", adapter = PayloadTypeAdapter::class)
sealed class Payload(
    val type: String
)

class PayloadTypeAdapter : TypeAdapter<Payload> {
    override fun classFor(type: Any): KClass<out Payload> =
        when (type as String) {
            "v0_CreateTransactionOrder" -> TransactionOrderPayload::class
            "v0_CreateTransferOrder" -> TransferOrderPayload::class
            "v0_ValidateTickers" -> ValidateTickersPayload::class
            else -> throw IllegalArgumentException("Unknown type: $type")
        }
}