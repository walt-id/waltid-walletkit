package id.walt.gateway.providers.metaco.restapi.balance.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.balance.model.Balance
import kotlinx.serialization.Serializable

@Serializable
data class BalanceList(
    val items: List<Balance>,
    val count: Int,
    @Json(serializeNull = false)
    val currentStartingAfter: String?,
    @Json(serializeNull = false)
    val nextStartingAfter: String?
)