package id.walt.verifier.backend

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import id.walt.model.siopv2.*
import java.net.URL
import java.nio.file.Path
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

object SIOPv2RequestManager {
    val reqCache: Cache<String, SIOPv2Request> = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build()

    fun newRequest(schemaUri: String): SIOPv2Request {
        val nonce = UUID.randomUUID().toString()
        val req = SIOPv2Request(
            client_id = "${VerifierConfig.config.verifierApiUrl}/verify/$nonce",
            redirect_uri = "${VerifierConfig.config.verifierApiUrl}/verify/$nonce",
            nonce = nonce,
            registration = Registration(
                client_name = "Walt.id Verifier Portal",
                client_purpose = "Verification of ${Path.of(URL(schemaUri).path).fileName}"
            ),
            expiration = Instant.now().epochSecond + 24 * 60 * 60,
            issuedAt = Instant.now().epochSecond,
            claims = Claims(
                vp_token = VpTokenClaim(
                    presentation_definition = PresentationDefinition(
                        id = "1",
                        input_descriptors = listOf(
                            InputDescriptor(
                                id = "1",
                                schema = schemaUri
                            )
                        )
                    )
                )
            )
        )
        reqCache.put(nonce, req)
        return req
    }
}
