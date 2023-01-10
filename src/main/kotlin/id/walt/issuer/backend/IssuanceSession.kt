package id.walt.issuer.backend

import com.nimbusds.oauth2.sdk.AuthorizationRequest
import id.walt.model.oidc.CredentialAuthorizationDetails

data class IssuanceSession(
    val id: String,
    val credentialDetails: List<CredentialAuthorizationDetails>,
    val nonce: String,
    val isPreAuthorized: Boolean,
    var authRequest: AuthorizationRequest?,
    var issuables: Issuables?,
    var did: String? = null,
    val userPin: String? = null,
    var issuerDid: String? = null
)
