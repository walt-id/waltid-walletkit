package id.walt.webwallet.backend.oidc

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.JsonProperty
import id.walt.webwallet.backend.oidc.requests.OidcRegisterRequest
import java.util.*

data class OidcClient(
    val _key: String,

    // REQUIRED
    @JsonProperty("client_id") val clientId: String,
    @JsonProperty("client_secret_expires_at") val clientSecretExpiresAt: Long,
    @JsonProperty("redirect_uris") val redirectUris: List<String>,

    // OPTIONAL
    @JsonProperty("client_secret") @JsonInclude(NON_EMPTY) val clientSecret: String = "",
    @JsonProperty("registration_access_token") @JsonInclude(NON_EMPTY) val registrationAccessToken: String = "",
    @JsonProperty("registration_client_uri") @JsonInclude(NON_EMPTY) val registrationClientUri: String = "",
    @JsonProperty("client_id_issued_at") @JsonInclude(NON_EMPTY) val clientIdIssuedAt: String = "",

    // Client Metadata - same as at : OidcRegisterRequest
    @JsonProperty("application_type") val applicationType: String? = "",
    @JsonProperty("client_name") val clientName: String? = "",
    @JsonProperty("logo_uri") val logoUri: String? = "",
    @JsonProperty("subject_type") val subjectType: String? = "",
    @JsonProperty("sector_identifier_uri") val sectorIdentifierUri: String? = "",
    @JsonProperty("token_endpoint_auth_method") val tokenEndpointAuthMethod: String? = "",
    @JsonProperty("jwks_uri") val jwksUri: String? = "",
    @JsonProperty("userinfo_encrypted_response_alg") val userinfoEncryptedResponseAlg: String? = "",
    @JsonProperty("userinfo_encrypted_response_enc") val userinfoEncryptedResponseEnc: String? = "",
    @JsonProperty("contacts") val contacts: List<String> = emptyList(),
    @JsonProperty("request_uris") val requestUris: List<String> = emptyList()
) {
    companion object {
        fun newClient(req: OidcRegisterRequest): OidcClient {
            val clientID = UUID.randomUUID().toString()
            return OidcClient(
                _key = clientID,
                clientId = clientID,
                clientSecretExpiresAt = 0, // will not expire
                redirectUris = req.redirectUris,
                clientSecret = UUID.randomUUID().toString(),
                registrationAccessToken = "",
                registrationClientUri = "",
                clientIdIssuedAt = "", // TODO should be current date in following UTC format 1970-01-01T0:0:0Z
                applicationType = req.applicationType,
                clientName = req.clientName,
                logoUri = req.logoUri,
                subjectType = req.subjectType,
                sectorIdentifierUri = req.sectorIdentifierUri,
                tokenEndpointAuthMethod = req.tokenEndpointAuthMethod,
                jwksUri = req.jwksUri,
                userinfoEncryptedResponseAlg = req.userinfoEncryptedResponseAlg,
                userinfoEncryptedResponseEnc = req.userinfoEncryptedResponseEnc,
                contacts = req.contacts,
                requestUris = req.requestUris
            )
        }
    }
}
