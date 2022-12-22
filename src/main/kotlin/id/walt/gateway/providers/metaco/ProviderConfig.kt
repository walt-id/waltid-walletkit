package id.walt.gateway.providers.metaco

import java.io.File

object ProviderConfig {
    val gatewayUrl: String = System.getenv("GATEWAY_URL") ?: ""
    val oauthUrl: String = System.getenv("OAUTH_URL") ?: ""
    val oauthClientId: String = System.getenv("OAUTH_CLIENT_ID") ?: ""
    val oauthClientSecret: String = System.getenv("OAUTH_CLIENT_SECRET") ?: ""
    val userId: String = System.getenv("USER_ID") ?: ""
    val domainId: String = System.getenv("DOMAIN_ID") ?: ""
    val publicKey: String = System.getenv("PUBLIC_KEY") ?: ""
    val grantType: String = System.getenv("GRANT_TYPE") ?: ""
    val signServiceUrl: String = System.getenv("SIGN_SERVICE_URL") ?: ""
    private val privateKeyPath: String = System.getenv("PRIVATE_KEY_PATH") ?: ""
    val privateKey: String = File(privateKeyPath).takeIf { it.exists() }?.readText() ?: ""
}