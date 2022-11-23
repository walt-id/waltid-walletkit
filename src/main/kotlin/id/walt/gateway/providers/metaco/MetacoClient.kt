package id.walt.gateway.providers.metaco

//object MetacoClient {
//    private val signer = WaltIdSandboxSigner()
//    private val authenticator = WaltIdSandboxAuthenticator()

//    private val config = Config.createConfig(
//        mapOf(
//            "GATEWAY_URL" to ProviderConfig.gatewayUrl,
//            "OAUTH_URL" to ProviderConfig.oauthUrl,
//            "OAUTH_CLIENT_ID" to ProviderConfig.oauthClientId,
//            "OAUTH_CLIENT_SECRET" to ProviderConfig.oauthClientSecret,
//            "USER_ID" to ProviderConfig.userId,
//        )
//    )
//    val harmonize: Harmonize by lazy {
//        Harmonize.open(
//            Context.DefaultContext(
//                config,
//                authenticator,
//                signer,
//                Requests.defaultHttpRequests(config),
//                Logger.getGlobal()
//            )
//        )
//    }
//}