package id.walt.gateway.providers.metaco.restapi.transfer.model.transferparty

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
@TypeFor(field = "type", adapter = TransferPartyAdapter::class)
sealed class TransferParty(
    val type: String
)

class TransferPartyAdapter : TypeAdapter<TransferParty> {
    override fun classFor(type: Any): KClass<out TransferParty> = when (type as String) {
        "Address" -> AddressTransferParty::class
        "Account" -> AccountTransferParty::class
        else -> throw IllegalArgumentException("Unknown type: $type")
    }
}