package id.walt.webwallet.backend.wallet

import com.beust.klaxon.Json

data class DidCreationRequest(
  @Json(serializeNull = false) val bearerToken: String? = null,
  @Json(serializeNull = false) val didWebDomain: String? = null,
  @Json(serializeNull = false) val didWebPath: String? = null,
  @Json(serializeNull = false) val keyId: String? = null,
)
