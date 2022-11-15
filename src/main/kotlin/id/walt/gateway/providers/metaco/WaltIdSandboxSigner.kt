package id.walt.gateway.providers.metaco

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

class WaltIdSandboxSigner: Signer {
    private val signServiceUrl = "sign-service"

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
            setBody(mapOf("payload" to p1, "privateKey" to MetacoClient.privKey))
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

    override fun getPublicKey() = MetacoClient.pubKey

    override fun getAuthor() = Author(MetacoClient.userId, MetacoClient.domainId)

    @Serializable
    data class SignChallengeResponse(
        val canonicalPayload: String,
        val hash: String,
        val signature: String,
    )
}