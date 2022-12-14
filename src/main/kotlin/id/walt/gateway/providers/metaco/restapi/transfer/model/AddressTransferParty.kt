package id.walt.gateway.providers.metaco.restapi.transfer.model

import kotlinx.serialization.Serializable

@Serializable
class AddressTransferParty(
    val address: String,
    val addressDetails: AddressDetails,
) : TransferParty("Address")