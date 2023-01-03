package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.providers.metaco.restapi.services.AuthService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*

abstract class BaseRestRepository(
    open val authService: AuthService
) {
    protected val baseUrl = ProviderConfig.gatewayUrl
    private val bearerTokenStorage = mutableListOf<BearerTokens>()

    protected val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
        install(Auth) {
            bearer {
                loadTokens {
                    fetchAuthToken()
                }
                refreshTokens {
                    fetchAuthToken()
                }
            }
        }
    }

    private fun fetchAuthToken(): BearerTokens {
        authService.authorize().run {
            bearerTokenStorage.add(BearerTokens(this.accessToken, this.accessToken))
        }
        return bearerTokenStorage.last()
    }
}