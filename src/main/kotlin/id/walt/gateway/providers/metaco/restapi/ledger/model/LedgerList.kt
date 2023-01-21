package id.walt.gateway.providers.metaco.restapi.ledger.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class LedgerList(
    val items: List<Ledger>,
    val count: Int,
    @Json(serializeNull = false)
    val currentStartingAfter: String? = null,
    @Json(serializeNull = false)
    val nextStartingAfter: String? = null,
)