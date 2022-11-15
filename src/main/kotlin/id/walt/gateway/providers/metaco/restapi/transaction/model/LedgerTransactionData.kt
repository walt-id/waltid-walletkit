package id.walt.gateway.providers.metaco.restapi.transaction.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.transaction.model.LedgerData
import kotlinx.serialization.Serializable

@Serializable
data class LedgerTransactionData(
    @Json(serializeNull = false)
    val failure: String?,
    @Json(serializeNull = false)
    val ledgerData: LedgerData?,
    @Json(serializeNull = false)
    val ledgerStatus: String?,
    @Json(serializeNull = false)
    val ledgerTransactionId: String?,
    @Json(serializeNull = false)
    val rawTransaction: String?,
    @Json(serializeNull = false)
    val statusLastUpdatedAt: String?
)