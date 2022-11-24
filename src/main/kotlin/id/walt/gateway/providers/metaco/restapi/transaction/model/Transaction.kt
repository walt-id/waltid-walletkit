package id.walt.gateway.providers.metaco.restapi.transaction.model

import com.beust.klaxon.Json
import id.walt.gateway.providers.metaco.restapi.transaction.model.LedgerTransactionData
import id.walt.gateway.providers.metaco.restapi.transaction.model.OrderReference
import id.walt.gateway.providers.metaco.restapi.transaction.model.Processing
import id.walt.gateway.providers.metaco.restapi.transaction.model.RelatedAccount
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    @Json(serializeNull = false)
    val ledgerId: String?,
    @Json(serializeNull = false)
    val ledgerTransactionData: LedgerTransactionData?,
    @Json(serializeNull = false)
    val orderReference: OrderReference?,
    @Json(serializeNull = false)
    val processing: Processing?,
    val registeredAt: String,
    val relatedAccounts: ArrayList<RelatedAccount>
)