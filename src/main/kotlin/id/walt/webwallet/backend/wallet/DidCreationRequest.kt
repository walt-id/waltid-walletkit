package id.walt.webwallet.backend.wallet

import com.beust.klaxon.Json

data class DidCreationRequest(
  @Json(serializeNull = false) val bearerToken: String? = null
)
