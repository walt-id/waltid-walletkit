package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.CommonHttp
import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.providers.metaco.restapi.models.EntityList
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

    val client = HttpClient(CIO) {
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

    inline fun <reified K: EntityList<T>, T> findAllLoopPages(url: String, criteria: Map<String, String>): List<T> = let {
        val list = mutableListOf<T>()
        var entityList = CommonHttp.get<K>(client, url.plus(CommonHttp.buildQueryList(criteria)))
        do {
            list.addAll(entityList.items)
            entityList.nextStartingAfter?.let{
                entityList = CommonHttp.get<K>(client, url.plus(CommonHttp.buildQueryList(criteria.plus("startingAfter" to it))))
            }
        } while (entityList.nextStartingAfter != null)
        list
    }

    private fun fetchAuthToken(): BearerTokens {
        authService.authorize().run {
            bearerTokenStorage.add(BearerTokens(this.accessToken, this.accessToken))
        }
        return bearerTokenStorage.last()
    }
}