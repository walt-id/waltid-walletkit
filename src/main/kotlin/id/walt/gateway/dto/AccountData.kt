package id.walt.gateway.dto

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
data class AccountData(
    val accountId: String,
    val tickers: List<String>,
    val addresses: List<String>,
    @Json(serializeNull = false)
    val alias: String? = null,
)
