package id.walt.gateway.providers.metaco.restapi.intent.model.intent.fee

import kotlinx.serialization.Serializable

@Serializable
abstract class FeeStrategy {
    abstract val type: String
}