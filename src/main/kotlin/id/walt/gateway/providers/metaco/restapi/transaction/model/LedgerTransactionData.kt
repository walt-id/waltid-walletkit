package id.walt.gateway.providers.metaco.restapi.transaction.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.transaction.model.ledgerdata.LedgerData
import kotlinx.serialization.Serializable

@Serializable
data class LedgerTransactionData(
    val ledgerStatus: String,
    @Json(serializeNull = false)
    val failure: String? = null,
    val ledgerTransactionId: String,
    @Json(serializeNull = false)
    val rawTransaction: String? = null,
    val statusLastUpdatedAt: String,
    @Json(serializeNull = false)
    val ledgerData: LedgerData? = null,
)