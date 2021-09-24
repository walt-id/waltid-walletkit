package id.walt.webwallet.backend.oidc.requests

import com.fasterxml.jackson.annotation.JsonProperty

// https://openid.net/specs/openid-connect-registration-1_0.html#ClientMetadata
data class OidcRegisterRequest(
    @JsonProperty("application_type") val applicationType: String?,
    @JsonProperty("redirect_uris") val redirectUris: List<String>,
    @JsonProperty("client_name") val clientName: String?,
    @JsonProperty("logo_uri") val logoUri: String?,
    @JsonProperty("subject_type") val subjectType: String?,
    @JsonProperty("sector_identifier_uri") val sectorIdentifierUri: String?,
    @JsonProperty("token_endpoint_auth_method") val tokenEndpointAuthMethod: String?,
    @JsonProperty("jwks_uri") val jwksUri: String?,
    @JsonProperty("userinfo_encrypted_response_alg") val userinfoEncryptedResponseAlg: String?,
    @JsonProperty("userinfo_encrypted_response_enc") val userinfoEncryptedResponseEnc: String?,
    @JsonProperty("contacts") val contacts: List<String> = emptyList(),
    @JsonProperty("request_uris") val requestUris: List<String> = emptyList()
)
