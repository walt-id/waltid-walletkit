package id.walt.gateway.providers.metaco.restapi.transaction.model.ledgerdata

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
@TypeFor(field = "type", adapter = LedgerDataTypeAdapter::class)
sealed class LedgerData(
    val type: String,
)

class LedgerDataTypeAdapter : TypeAdapter<LedgerData> {
    override fun classFor(type: Any): KClass<out LedgerData> =
        when (type as String) {
            "Ethereum" -> EthereumLedgerData::class
            "Bitcoin" -> BitcoinLedgerData::class
            else -> throw IllegalArgumentException("Unknown type: $type")
        }
}