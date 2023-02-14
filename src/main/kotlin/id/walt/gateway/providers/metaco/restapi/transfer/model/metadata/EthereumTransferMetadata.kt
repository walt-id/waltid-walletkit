package id.walt.gateway.providers.metaco.restapi.transfer.model.metadata

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
class EthereumTransferMetadata(
    @Json(serializeNull = false)
    val dummy: String? = null,
) : TransferMetadata("Ethereum")
