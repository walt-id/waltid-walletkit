package id.walt.webwallet.backend.wallet

import com.beust.klaxon.Json
import id.walt.model.DidMethod

data class DidCreationRequest(
    val method: DidMethod = DidMethod.key,
    @Json(serializeNull = false) val keyId: String? = null,
    @Json(serializeNull = false) val didEbsiBearerToken: String? = null,
    @Json(serializeNull = false) val didWebDomain: String? = null,
    @Json(serializeNull = false) val didWebPath: String? = null,
    @Json(serializeNull = false) val didEbsiVersion: Int = 1,
)
