package id.walt.gateway.providers.metaco

import com.metaco.harmonize.Harmonize
import com.metaco.harmonize.conf.Config
import com.metaco.harmonize.conf.Context
import com.metaco.harmonize.net.Requests
import java.util.logging.Logger
import kotlin.streams.asSequence

object MetacoClient {
    const val unknownValue = "unknown"
    const val externalValue = "external"

    val domainId = "domain-id"
    val userId = "user-id"
    val pubKey = "pub-key"
    val privKey = "priv-key"

    private val signer = WaltIdSandboxSigner()
    private val authenticator = WaltIdSandboxAuthenticator()
    private val config = Config.createConfig(mapOf(
        "GATEWAY_URL" to "api-url",
        "OAUTH_URL" to "oauth-url",
        "OAUTH_CLIENT_ID" to "client-id",
        "OAUTH_CLIENT_SECRET" to "secret",
        "USER_ID" to "user-id",
    ))
    val harmonize: Harmonize by lazy {
        Harmonize.open(
            Context.DefaultContext(
                config,
                authenticator,
                signer,
                Requests.defaultHttpRequests(config),
                Logger.getGlobal()
            )
        )
    }

    fun domains() {
        println(harmonize.domains().list().asSequence().joinToString {
            it.asJson()
        })
    }
}