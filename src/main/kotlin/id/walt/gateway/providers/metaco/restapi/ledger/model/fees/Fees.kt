package id.walt.gateway.providers.metaco.restapi.ledger.model.fees

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import id.walt.gateway.providers.metaco.restapi.ledger.model.fees.ethereum.EthereumFees
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
@TypeFor(field = "type", adapter = FeeTypeAdapter::class)
abstract class Fees(
    val type: String
)

class FeeTypeAdapter : TypeAdapter<Fees> {
    override fun classFor(type: Any): KClass<out Fees> = when (type as String) {
        "Ethereum" -> EthereumFees::class
        else -> throw IllegalArgumentException("No fee for $type")
    }

}