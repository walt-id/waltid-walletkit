package id.walt.gateway.providers.metaco.restapi.transaction.model

import id.walt.gateway.providers.metaco.restapi.transaction.model.AccountReference
import kotlinx.serialization.Serializable

@Serializable
data class Output(
    val accountReference: AccountReference,
    val address: String,
    val amount: String,
    val index: Int,
    val scriptPubKey: String
)