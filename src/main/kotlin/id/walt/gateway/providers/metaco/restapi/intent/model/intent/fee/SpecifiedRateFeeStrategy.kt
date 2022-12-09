package id.walt.gateway.providers.metaco.restapi.intent.model.intent.fee

import kotlinx.serialization.Serializable

@Serializable
data class SpecifiedRateFeeStrategy(
    val gasPrice: String,
) : FeeStrategy() {
    override val type = "SpecifiedRate"
}