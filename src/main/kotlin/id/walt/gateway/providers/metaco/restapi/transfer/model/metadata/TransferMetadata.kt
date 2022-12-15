package id.walt.gateway.providers.metaco.restapi.transfer.model.metadata

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
@TypeFor(field = "type", adapter = MetadataTypeAdapter::class)
sealed class TransferMetadata(
    val type: String,
)

class MetadataTypeAdapter : TypeAdapter<TransferMetadata> {
    override fun classFor(type: Any): KClass<out TransferMetadata> =
        when (type as String) {
            "Ethereum" -> EthereumTransferMetadata::class
            "Bitcoin" -> BitcoinTransferMetadata::class
            else -> throw IllegalArgumentException("Unknown type: $type")
        }
}