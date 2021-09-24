package id.walt.webwallet.backend.oidc.requests

data class OidcTokenRequest(
    var grantType: String,
    var code: String,
    var redirectUri: String
)
