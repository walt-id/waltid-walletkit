package id.walt.gateway.providers.metaco.restapi.intent.model.payload

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
@TypeFor(field = "type", adapter = PayloadTypeAdapter::class)
sealed class Payload(
    val type: String,
) {
    enum class Types(val value: String) {
        CreateTransactionOrder("v0_CreateTransactionOrder"),
        CreateTransferOrder("v0_CreateTransferOrder"),
        ValidateTickers("v0_ValidateTickers"),
        ReleaseQuarantinedTransfers("v0_ReleaseQuarantinedTransfers"),
        CreateAccount("v0_CreateAccount"),
    }
}

class PayloadTypeAdapter : TypeAdapter<Payload> {
    override fun classFor(type: Any): KClass<out Payload> =
        when (type as String) {
            Payload.Types.CreateTransactionOrder.value -> TransactionOrderPayload::class
            Payload.Types.CreateTransferOrder.value -> TransferOrderPayload::class
            Payload.Types.ValidateTickers.value -> ValidateTickersPayload::class
            Payload.Types.CreateAccount.value -> CreateAccountPayload::class
            else -> throw IllegalArgumentException("Unknown type: $type")
        }
}