package id.walt.gateway.providers.metaco.restapi.ledger.model.fees

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import id.walt.gateway.providers.metaco.restapi.ledger.model.fees.bitcoin.BitcoinFees
import id.walt.gateway.providers.metaco.restapi.ledger.model.fees.ethereum.EthereumFees
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
@TypeFor(field = "type", adapter = FeeTypeAdapter::class)
abstract class Fees(
    val type: String,
) {
    abstract val high: PriorityStrategy
    abstract val medium: PriorityStrategy
    abstract val low: PriorityStrategy
}

class FeeTypeAdapter : TypeAdapter<Fees> {
    override fun classFor(type: Any): KClass<out Fees> = when (type as String) {
        "Ethereum" -> EthereumFees::class
        "Bitcoin" -> BitcoinFees::class
        else -> throw IllegalArgumentException("No fee type defined for $type")
    }

}

fun Fees.priorityStrategyFromString(priority: String) = when (priority.lowercase()) {
    "high" -> this.high
    "medium" -> this.medium
    "low" -> this.low
    else -> this.low
}