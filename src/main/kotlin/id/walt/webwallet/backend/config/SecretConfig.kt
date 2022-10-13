package id.walt.webwallet.backend.config

import id.walt.model.oidc.OIDCProvider

data class SecretConfig(
    val client_id: String,
    val client_secret: String
)

data class SecretConfigMap(
    val secrets: Map<String, SecretConfig>
)

fun OIDCProvider.withSecret(secretConfig: SecretConfig?): OIDCProvider =
    OIDCProvider(
        this.id,
        this.url,
        this.description,
        secretConfig?.client_id ?: this.client_id,
        secretConfig?.client_secret ?: this.client_secret
    )
