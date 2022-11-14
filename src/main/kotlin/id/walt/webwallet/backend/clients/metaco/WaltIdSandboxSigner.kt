package id.walt.webwallet.backend.clients.metaco

import com.metaco.harmonize.api.om.Author
import com.metaco.harmonize.conf.Context
import com.metaco.harmonize.msg.JSON
import com.metaco.harmonize.sig.Signature
import com.metaco.harmonize.sig.Signer
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

class WaltIdSandboxSigner: Signer {
    val userId = "userId"
    val domainId = "domainId"
    val pubKey = "pubKey"
    val privKey = "privKey"
    val signServiceUrl = "signServiceUrl"

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }

    override fun signChallenge(p0: Context?, p1: String?) = runBlocking {
        val response = client.post(signServiceUrl) {
            contentType(ContentType.Application.Json)
            setBody(mapOf("payload" to p1, "privateKey" to privKey))
        }.body<SignChallengeResponse>()
        Signature(
            mapOf(
                "canonicalPayload" to response.canonicalPayload,
                "hash" to response.hash,
                "signature" to response.signature
            )
        )
    }

    override fun signPayload(p0: Context?, p1: JSON?): Signature {
        TODO("Not yet implemented")
    }

    override fun getPublicKey() = pubKey

    override fun getAuthor() = Author(userId, domainId)

    @Serializable
    data class SignChallengeRequest(
        val payload: String,
        val privateKey: String,
    )

    @Serializable
    data class SignChallengeResponse(
        val canonicalPayload: String,
        val hash: String,
        val signature: String,
    )
}