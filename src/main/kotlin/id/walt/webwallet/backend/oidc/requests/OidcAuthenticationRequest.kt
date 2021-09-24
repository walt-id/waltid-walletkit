package id.walt.webwallet.backend.oidc.requests

// https://openid.net/specs/openid-connect-core-1_0.html#AuthRequest
data class OidcAuthenticationRequest(
    var scope: String?,
    var responseType: String?,
    var clientId: String?,
    var redirectUri: String?,
    var state: String?
)
