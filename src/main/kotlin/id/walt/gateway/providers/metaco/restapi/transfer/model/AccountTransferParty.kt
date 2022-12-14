package id.walt.gateway.providers.metaco.restapi.transfer.model

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
class AccountTransferParty(
    val domainId: String,
    val accountId: String,
    @Json(serializeNull = false)
    val addressDetails: AddressDetails? = null,
) : TransferParty("Account")

