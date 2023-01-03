package id.walt.gateway.providers.metaco.restapi.ticker.model.ledgerproperties

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
@TypeFor(field = "type", adapter = LedgerDetailsPropertiesTypeAdapter::class)
sealed class LedgerProperties(
    val type: String
)

class LedgerDetailsPropertiesTypeAdapter : TypeAdapter<LedgerProperties> {
    override fun classFor(type: Any): KClass<out LedgerProperties> =
        when (type as String) {
            "ERC20" -> ERC20LedgerProperties::class
            "ERC721" -> ERC721LedgerProperties::class
            "Native" -> NativeLedgerProperties::class
            else -> throw IllegalArgumentException("Unknown type: $type")
        }
}