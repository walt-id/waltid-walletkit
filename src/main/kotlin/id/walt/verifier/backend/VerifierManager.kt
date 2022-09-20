package id.walt.verifier.backend

import com.beust.klaxon.JsonObject
import com.google.common.cache.CacheBuilder
import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.nimbusds.oauth2.sdk.ResponseMode
import com.nimbusds.oauth2.sdk.id.State
import com.nimbusds.openid.connect.sdk.Nonce
import id.walt.model.oidc.VpTokenClaim
import id.walt.services.context.ContextManager
import id.walt.services.did.DidService
import id.walt.services.hkvstore.FileSystemHKVStore
import id.walt.services.hkvstore.FilesystemStoreConfig
import id.walt.services.jwt.JwtService
import id.walt.services.key.KeyService
import id.walt.services.keystore.HKVKeyStoreService
import id.walt.services.vcstore.HKVVcStoreService
import id.walt.WALTID_DATA_ROOT
import id.walt.auditor.*
import id.walt.model.dif.*
import id.walt.model.oidc.VCClaims
import id.walt.servicematrix.BaseService
import id.walt.servicematrix.ServiceRegistry
import id.walt.services.context.Context
import id.walt.services.oidc.OIDC4VPService
import id.walt.vclib.credentials.VerifiablePresentation
import id.walt.vclib.model.toCredential
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.auth.UserInfo
import id.walt.webwallet.backend.context.UserContext
import io.javalin.http.BadRequestResponse
import java.net.URI
import java.util.*
import java.util.concurrent.TimeUnit

abstract class VerifierManager: BaseService() {
  val reqCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build<String, AuthorizationRequest>()
  val respCache =
    CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build<String, SIOPResponseVerificationResult>()

  abstract val verifierContext: Context

  open fun newRequest(walletUrl: URI, presentationDefinition: PresentationDefinition, state: String? = null, redirectCustomUrlQuery: String = ""): AuthorizationRequest {
    val nonce = UUID.randomUUID().toString()
    val requestId = state ?: nonce
    val redirectQuery = if(redirectCustomUrlQuery.isEmpty()) "" else "?$redirectCustomUrlQuery"
    val req = OIDC4VPService.createOIDC4VPRequest(
      wallet_url = walletUrl,
      redirect_uri = URI.create("${VerifierConfig.config.verifierApiUrl}/verify$redirectQuery"),
      response_mode = ResponseMode.FORM_POST,
      nonce = Nonce(nonce),
      presentation_definition = presentationDefinition,
      state = State(requestId)
    )
    reqCache.put(requestId, req)
    return req
  }

  open fun newRequestBySchemaUris(walletUrl: URI, schemaUris: Set<String>, state: String? = null, redirectCustomUrlQuery: String = ""): AuthorizationRequest {
    return newRequest(walletUrl, PresentationDefinition(
        id = "1",
        input_descriptors = schemaUris.map { schemaUri ->
          InputDescriptor(
            id = "1",
            schema = VCSchema(uri = schemaUri)
          )
        }.toList()
      ), state, redirectCustomUrlQuery)
  }

  open fun newRequestByVcTypes(walletUrl: URI, vcTypes: Set<String>, state: String? = null, redirectCustomUrlQuery: String = ""): AuthorizationRequest {
    return newRequest(walletUrl, PresentationDefinition(
      id = "1",
      input_descriptors = vcTypes.map { vcType ->
        InputDescriptor(
          id = "1",
          constraints = InputDescriptorConstraints(
            fields = listOf(
              InputDescriptorField(
                path = listOf("$.type"),
                filter = mapOf(
                  "const" to vcType
                )
              )
            )
          )
        )
      }.toList()
    ), state, redirectCustomUrlQuery)
  }

  open fun getVerififactionPoliciesFor(req: AuthorizationRequest): List<VerificationPolicy> {
    return listOf(
      SignaturePolicy(),
      ChallengePolicy(req.getCustomParameter("nonce")!!.first(), applyToVC = false, applyToVP = true),
      PresentationDefinitionPolicy(OIDC4VPService.getPresentationDefinition(req)),
      *(VerifierConfig.config.additionalPolicies?.map { p ->
        PolicyRegistry.getPolicyWithJsonArg(p.policy, p.argument?.let { JsonObject(it) })
      }?.toList() ?: listOf()).toTypedArray()
    )
  }

  /*
  - find cached request
  - parse and verify id_token
  - parse and verify vp_token
  -  - compare nonce (verification policy)
  -  - compare token_claim => token_ref => vp (verification policy)
   */
  open fun verifyResponse(state: String, vp_token: String): SIOPResponseVerificationResult {
    val req = reqCache.getIfPresent(state) ?: throw BadRequestResponse("State invalid or expired")
    reqCache.invalidate(state)
    val vp = vp_token.toCredential() as VerifiablePresentation

    var result = SIOPResponseVerificationResult(
      state,
      vp.holder,
      req,
      ContextManager.runWith(verifierContext) {

        Auditor.getService().verify(
          vp_token, getVerififactionPoliciesFor(req)
        )
      },
      vp_token = vp,
      null
    )

    if (result.isValid) {
      result.auth_token = JWTService.toJWT(UserInfo(result.subject!!))
    }

    respCache.put(result.state, result)

    return result
  }

  open fun getVerificationRedirectionUri(verificationResult: SIOPResponseVerificationResult, uiUrl: String? = VerifierConfig.config.verifierUiUrl): URI {
    if(verificationResult.isValid == true)
      return URI.create("$uiUrl/success/?access_token=${verificationResult.state}")
    else
      return URI.create("$uiUrl/error/?access_token=${verificationResult.state ?: ""}")
  }

  fun getVerificationResult(id: String): SIOPResponseVerificationResult? {
    return respCache.getIfPresent(id).also {
      respCache.invalidate(id)
    }
  }

  override val implementation: BaseService
    get() = serviceImplementation<VerifierManager>()

  companion object {
    fun getService(): VerifierManager = ServiceRegistry.getService()
  }
}

class DefaultVerifierManager : VerifierManager() {
  override val verifierContext = UserContext(
    contextId = "Verifier",
    hkvStore = FileSystemHKVStore(FilesystemStoreConfig("$WALTID_DATA_ROOT/data/verifier")),
    keyStore = HKVKeyStoreService(),
    vcStore = HKVVcStoreService()
  )

}
