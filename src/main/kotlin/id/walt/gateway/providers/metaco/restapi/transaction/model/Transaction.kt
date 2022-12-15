package id.walt.gateway.providers.metaco.restapi.transaction.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    val ledgerId: String,
    @Json(serializeNull = false)
    val orderReference: OrderReference?,
    val relatedAccounts: List<RelatedAccount>,
    @Json(serializeNull = false)
    val processing: Processing?,
    val registeredAt: String,
    @Json(serializeNull = false)
    val ledgerTransactionData: LedgerTransactionData?
)