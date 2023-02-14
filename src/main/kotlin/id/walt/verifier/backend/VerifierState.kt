package id.walt.verifier.backend

import com.google.common.cache.CacheBuilder
import com.nimbusds.oauth2.sdk.AuthorizationRequest
import id.walt.model.dif.PresentationDefinition
import id.walt.multitenancy.TenantState
import java.util.concurrent.*

class VerifierState : TenantState<VerifierConfig> {
    val reqCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build<String, AuthorizationRequest>()
    val respCache =
        CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build<String, SIOPResponseVerificationResult>()
    val presentationDefinitionCache =
        CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build<String, PresentationDefinition>()

    override var config: VerifierConfig? = null
}
