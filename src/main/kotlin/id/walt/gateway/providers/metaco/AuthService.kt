package id.walt.webwallet.backend.clients.metaco.services

import com.fasterxml.jackson.annotation.JsonProperty
import com.metaco.harmonize.sig.Signature
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
    private val userId = "user-id"
    private val rootDomainId = "domain-id"
    private val pubKey = "pub-key"
    private val privKey = "priv-key"
    private val signServiceUrl = "sign-service"
    private val authServiceUrl = "auth-service"

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
        val response = client.post(signServiceUrl) {
            contentType(ContentType.Application.Json)
            setBody(mapOf("payload" to payload, "privateKey" to privKey))
        }.body<SignChallengeResponse>()
        Signature(
            mapOf(
                "canonicalPayload" to response.canonicalPayload,
                "hash" to response.hash,
                "signature" to response.signature
            )
        )
    }

    private fun generateToken(signature: Signature) = runBlocking {
        client.submitForm(
            url = authServiceUrl,
            formParameters = Parameters.build {
                append("grant_type", "grant-type")
                append("client_id", "client-id")
                append("challenge", signature.canonicalPayload)
                append("signature", signature.signature)
                append("public_key", pubKey)
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