package id.walt.gateway.providers.metaco.restapi.intent.model.intent.fee

import kotlinx.serialization.Serializable

@Serializable
data class PriorityFeeStrategy(
    val priority: String,
) : FeeStrategy("Priority")