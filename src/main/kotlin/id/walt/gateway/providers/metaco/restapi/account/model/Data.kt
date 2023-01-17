package id.walt.gateway.providers.metaco.restapi.account.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable
import id.walt.gateway.providers.metaco.restapi.models.metadata.Metadata

@Serializable
data class Data(
    @Json(serializeNull = false)
    val alias: String? = null,
    val domainId: String,
    val id: String,
    val ledgerId: String,
    val lock: String,
    @Json(serializeNull = false)
    val metadata: Metadata? = null,
    val providerDetails: ProviderDetails
)