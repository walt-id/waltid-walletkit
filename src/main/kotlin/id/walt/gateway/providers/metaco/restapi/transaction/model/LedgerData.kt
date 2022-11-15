package id.walt.gateway.providers.metaco.restapi.transaction.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class LedgerData(
    @Json(serializeNull = false)
    val inputs: List<Input>?,
    @Json(serializeNull = false)
    val outputs: List<Output>?,
    @Json(serializeNull = false)
    val type: String?
)