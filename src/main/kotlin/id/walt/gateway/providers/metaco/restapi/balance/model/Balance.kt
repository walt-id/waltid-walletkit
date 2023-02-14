package id.walt.gateway.providers.metaco.restapi.balance.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class Balance(
    @Json(serializeNull = false)
    val accountReference: AccountReference?,
    @Json(serializeNull = false)
    val lastUpdatedAt: String?,
    val quarantinedAmount: String,
    val reservedAmount: String,
    val tickerId: String,
    val totalAmount: String
)
