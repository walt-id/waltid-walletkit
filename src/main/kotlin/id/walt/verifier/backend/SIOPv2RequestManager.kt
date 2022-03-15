package id.walt.verifier.backend

import com.google.common.cache.CacheBuilder
import id.walt.auditor.Auditor
import id.walt.auditor.ChallengePolicy
import id.walt.auditor.SignaturePolicy
import id.walt.auditor.VpTokenClaimPolicy
import id.walt.model.dif.InputDescriptor
import id.walt.model.dif.PresentationDefinition
import id.walt.model.dif.VpSchema
import id.walt.model.oidc.Registration
import id.walt.model.oidc.SIOPv2Request
import id.walt.model.oidc.VpTokenClaim
import id.walt.services.context.ContextManager
import id.walt.services.did.DidService
import id.walt.services.hkvstore.FileSystemHKVStore
import id.walt.services.hkvstore.FilesystemStoreConfig
import id.walt.services.jwt.JwtService
import id.walt.services.key.KeyService
import id.walt.services.keystore.HKVKeyStoreService
import id.walt.services.vcstore.HKVVcStoreService
import id.walt.vclib.credentials.VerifiablePresentation
import id.walt.vclib.model.toCredential
import id.walt.WALTID_DATA_ROOT
import id.walt.model.oidc.VCClaims
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.auth.UserInfo
import id.walt.webwallet.backend.context.UserContext
import java.net.URL
import java.nio.file.Path
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

object SIOPv2RequestManager {
    val reqCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build<String, SIOPv2Request>()
    val respCache =
        CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build<String, ResponseVerification>()

    val verifierContext = UserContext(
        contextId = "Verifier",
        hkvStore = FileSystemHKVStore(FilesystemStoreConfig("$WALTID_DATA_ROOT/data/verifier")),
        keyStore = HKVKeyStoreService(),
        vcStore = HKVVcStoreService()
    )

    fun newRequest(schemaUri: String): SIOPv2Request {
        val nonce = UUID.randomUUID().toString()
        val req = SIOPv2Request(
            redirect_uri = "${VerifierConfig.config.verifierApiUrl}/verify/$nonce",
            response_mode = "form_post",
            nonce = nonce,
            claims = VCClaims(
                vp_token = VpTokenClaim(
                    presentation_definition = PresentationDefinition(
                        id = "1",
                        input_descriptors = listOf(
                            InputDescriptor(
                                id = "1",
                                schema = VpSchema(uri = schemaUri)
                            )
                        )
                    )
                )
            )
        )
        reqCache.put(nonce, req)
        return req
    }

    /*
    - find cached request
    - parse and verify id_token
    - parse and verify vp_token
    -  - compare nonce (verification policy)
    -  - compare token_claim => token_ref => vp (verification policy)
     */
    fun verifyResponse(nonce: String, id_token: String, vp_token: String): ResponseVerification? {
        val req = reqCache.getIfPresent(nonce) ?: return null
        reqCache.invalidate(nonce)
        val respId = UUID.randomUUID().toString()
        val id_token_claims = JwtService.getService().parseClaims(id_token)!!
        val sub = id_token_claims.get("sub").toString()
        val vp = vp_token.toCredential() as VerifiablePresentation

        var result = ResponseVerification(
            respId,
            sub,
            req,
            ContextManager.runWith(verifierContext) {
                if (!KeyService.getService().hasKey(sub))
                    DidService.importKey(sub)
                JwtService.getService().verify(id_token)
            },
            ContextManager.runWith(verifierContext) {

                Auditor.getService().verify(
                    vp_token, listOf(
                        SignaturePolicy(),
                        ChallengePolicy(req.nonce!!).apply {
                            applyToVC = false
                        },
                        VpTokenClaimPolicy(req.claims.vp_token)
                    )
                )
            },
            vp_token = vp,
            null
        )

        if (result.isValid) {
            result.auth_token = JWTService.toJWT(UserInfo(result.subject!!))
        }

        respCache.put(result.id, result)

        return result
    }

    fun getVerificationResult(id: String): ResponseVerification? {
        return respCache.getIfPresent(id).also {
            respCache.invalidate(id)
        }
    }
}
