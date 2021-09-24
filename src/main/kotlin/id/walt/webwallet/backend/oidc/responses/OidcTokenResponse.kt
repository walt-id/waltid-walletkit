package id.walt.webwallet.backend.oidc.responses

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.JsonProperty

data class OidcTokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("refresh_token") @JsonInclude(NON_EMPTY) val refreshToken: String = "",
    @JsonProperty("expires_in") val expiresIn: Int,
    @JsonProperty("id_token") val idToken: String
)
