package id.walt.gateway.providers.metaco.restapi.transfer.model.metadata

import com.beust.klaxon.Json
import kotlinx.serialization.Serializable

@Serializable
class BitcoinTransferMetadata(
    @Json(serializeNull = false)
    val outputIndex: Int? = null,
) : TransferMetadata("Bitcoin")
