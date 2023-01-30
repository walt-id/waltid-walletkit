package id.walt.gateway.providers.metaco.restapi.intent.model

import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import id.walt.gateway.providers.metaco.restapi.models.customproperties.CustomPropertiesModel
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val author: Author,
    val expiryAt: String,
    val targetDomainId: String,
    val id: String,
    val payload: Payload,
    val description: String? = null,
    val customProperties: CustomPropertiesModel,
    val type: String
)