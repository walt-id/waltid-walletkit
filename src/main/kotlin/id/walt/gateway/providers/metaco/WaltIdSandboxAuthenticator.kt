package id.walt.gateway.providers.metaco

import com.metaco.harmonize.auth.Authenticator
import com.metaco.harmonize.auth.Authenticator.AuthenticationError
import com.metaco.harmonize.auth.Authenticator.TokenUserPair
import com.metaco.harmonize.conf.Config
import com.metaco.harmonize.conf.Context
import com.metaco.harmonize.conf.OAuthConfig
import com.metaco.harmonize.net.Requests
import com.metaco.harmonize.sig.Signature
import com.metaco.harmonize.utils.URLUtils
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class WaltIdSandboxAuthenticator: Authenticator {
    private val currentTokenRef: AtomicReference<TokenUserPair> = AtomicReference()

    override fun getToken(context: Context): Authenticator.Token {
        val currentToken = currentTokenRef.get()
        val publicKey: String = context.signer.publicKey

//        if (currentToken != null) {
//            if (currentToken.publicKey == publicKey) {
//                val t = currentToken.token
//                if (!t.isExpired) return t
//            }
//        }

        val config: Config = context.config
        val oAuthConfig: OAuthConfig =
            context.config.oauthConfig.orElseThrow { AuthenticationError("No OAuthConfig was provided") }

        // Step 1: challenge sign
        val challenge: String = UUID.randomUUID().toString()
        val signature: Signature = context.signer.signChallenge(context, challenge)
        val response: Requests.Response = context.requests.post(
            context.config,
            config.oauthURL,
            URLUtils.queryStringBuilder()
                .add("grant_type", oAuthConfig.grantType)
                .add("scope", oAuthConfig.scope)
                .add("client_id",oAuthConfig.clientId)
                .add("challenge", challenge)
                .add("signature", signature.signature)
                .add("public_key", publicKey).build(),
            "Content-Type", "application/x-www-form-urlencoded"
        )
        val token = Authenticator.parseToken(response)
        currentTokenRef.set(TokenUserPair(token, publicKey))
        if (!response.isOk) throw AuthenticationError("Error getting token: " + response.status + ", " + response.body + ", url:" + config.oauthURL)
        return token
    }
}