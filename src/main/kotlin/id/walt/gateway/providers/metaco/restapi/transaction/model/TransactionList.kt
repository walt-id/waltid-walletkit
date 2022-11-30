package id.walt.gateway.providers.metaco.restapi.transaction.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.transaction.model.Transaction
import kotlinx.serialization.Serializable

@Serializable
data class TransactionList(
    val items: List<Transaction>,
    val count: Int,
    @Json(serializeNull = false)
    val currentStartingAfter: String?,
    @Json(serializeNull = false)
    val nextStartingAfter: String?
)
