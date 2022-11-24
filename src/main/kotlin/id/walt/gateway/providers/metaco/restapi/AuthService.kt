package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.providers.metaco.ProviderConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

class AuthService {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }

    fun authorize() = generateToken(signPayload(UUID.randomUUID().toString()))

    private fun signPayload(payload: String) = runBlocking {
        client.post(ProviderConfig.signServiceUrl) {
            contentType(ContentType.Application.Json)
            setBody(mapOf("payload" to payload, "privateKey" to ProviderConfig.privateKey))
        }.body<SignChallengeResponse>()
    }

    private fun generateToken(signature: SignChallengeResponse) = runBlocking {
        client.submitForm(
            url = ProviderConfig.oauthUrl,
            formParameters = Parameters.build {
                append("grant_type", ProviderConfig.grantType)
                append("client_id", ProviderConfig.oauthClientId)
                append("challenge", signature.canonicalPayload)
                append("signature", signature.signature)
                append("public_key", ProviderConfig.publicKey)
            }).body<TokenResponse>()
    }

    @Serializable
    data class SignChallengeResponse(
        val canonicalPayload: String,
        val hash: String,
        val signature: String,
    )

    @Serializable
    data class TokenResponse(
        @SerialName("access_token")
        val accessToken: String
    )
}