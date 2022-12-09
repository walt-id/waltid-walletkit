package id.walt.gateway.providers.metaco.restapi.intent.model.intent.parameters

import id.walt.gateway.providers.metaco.restapi.intent.model.intent.fee.FeeStrategy
import kotlinx.serialization.Serializable

@Serializable
abstract class Parameters {
    abstract val feeStrategy: FeeStrategy
    abstract val maximumFee: String
    abstract val type: String
}