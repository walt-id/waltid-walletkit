package id.walt.gateway.providers.metaco.restapi.ledger.model

import id.walt.gateway.providers.metaco.restapi.ledger.model.parameters.Parameters
import id.walt.gateway.providers.metaco.restapi.models.metadata.Metadata
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    val id: String,
    val alias: String,
    val parameters: Parameters,
    val metadata: Metadata,
)
