package id.walt.issuer.backend

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.common.cache.CacheBuilder
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.nimbusds.oauth2.sdk.ResponseMode
import com.nimbusds.oauth2.sdk.id.State
import com.nimbusds.openid.connect.sdk.Nonce
import id.walt.crypto.KeyAlgorithm
import id.walt.model.DidMethod
import id.walt.model.dif.PresentationDefinition
import id.walt.services.context.ContextManager
import id.walt.services.did.DidService
import id.walt.services.ecosystems.essif.EssifClient
import id.walt.services.ecosystems.essif.didebsi.DidEbsiService
import id.walt.services.hkvstore.FileSystemHKVStore
import id.walt.services.hkvstore.FilesystemStoreConfig
import id.walt.services.hkvstore.HKVKey
import id.walt.services.key.KeyService
import id.walt.services.keystore.HKVKeyStoreService
import id.walt.services.vcstore.HKVVcStoreService
import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.signatory.Signatory
import id.walt.signatory.dataproviders.MergingDataProvider
import id.walt.vclib.credentials.VerifiablePresentation
import id.walt.WALTID_DATA_ROOT
import id.walt.auditor.Auditor
import id.walt.auditor.SignaturePolicy
import id.walt.model.DidUrl
import id.walt.model.oidc.CredentialAuthorizationDetails
import id.walt.model.oidc.CredentialRequest
import id.walt.model.oidc.IssuanceInitiationRequest
import id.walt.services.jwt.JwtService
import id.walt.services.oidc.OIDC4VPService
import id.walt.verifier.backend.WalletConfiguration
import id.walt.webwallet.backend.auth.UserInfo
import id.walt.webwallet.backend.context.UserContext
import id.walt.webwallet.backend.context.WalletContextManager
import io.javalin.http.BadRequestResponse
import javalinjwt.JWTProvider
import java.net.URI
import java.time.Duration
import java.util.*
import java.util.concurrent.*

const val URL_PATTERN = "^https?:\\/\\/(?!-.)[^\\s\\/\$.?#].[^\\s]*\$"
fun isSchema(typeOrSchema: String): Boolean {
  return Regex(URL_PATTERN).matches(typeOrSchema)
}

object IssuerManager {

  val issuerContext = UserContext(
    contextId = "Issuer",
    hkvStore = FileSystemHKVStore(FilesystemStoreConfig("$WALTID_DATA_ROOT/data/issuer")),
    keyStore = HKVKeyStoreService(),
    vcStore = HKVVcStoreService()
  )
  val EXPIRATION_TIME: Duration = Duration.ofMinutes(5)
  val nonceCache =
    CacheBuilder.newBuilder().expireAfterWrite(EXPIRATION_TIME.seconds, TimeUnit.SECONDS).build<String, Boolean>()
  val sessionCache =
    CacheBuilder.newBuilder().expireAfterAccess(EXPIRATION_TIME.seconds, TimeUnit.SECONDS).build<String, IssuanceSession>()
  lateinit var issuerDid: String;

  val authCodeSecret = System.getenv("WALTID_ISSUER_AUTH_CODE_SECRET") ?: UUID.randomUUID().toString()
  val algorithm: Algorithm = Algorithm.HMAC256(authCodeSecret)

  val authCodeProvider = JWTProvider(
    algorithm,
    { session: IssuanceSession, alg: Algorithm? ->
      JWT.create().withSubject(session.id).withClaim("pre-authorized", session.isPreAuthorized).sign(alg)
    },
    JWT.require(algorithm).build()
  )

  init {
    WalletContextManager.runWith(issuerContext) {
      issuerDid = IssuerConfig.config.issuerDid ?: DidService.listDids().firstOrNull() ?: DidService.create(DidMethod.key)
    }
  }

  fun listIssuableCredentialsFor(user: String): Issuables {
    return Issuables(
      credentials = listOf(
        "VerifiableId",
        "VerifiableDiploma",
        "VerifiableVaccinationCertificate",
        "ProofOfResidence",
        "ParticipantCredential",
        "Europass",
        "OpenBadgeCredential"
      )
        .map { IssuableCredential.fromTemplateId(it) }
    )
  }

  private fun prompt(prompt: String, default: String?): String? {
    print("$prompt [$default]: ")
    val input = readLine()
    return when (input.isNullOrBlank()) {
      true -> default
      else -> input
    }
  }

  fun initializeInteractively() {
    val method = prompt("DID method ('key' or 'ebsi') [key]", "key")
    if (method == "ebsi") {
      val token = prompt("EBSI bearer token: ", null)
      if (token.isNullOrEmpty()) {
        println("EBSI bearer token required, to register EBSI did")
        return
      }
      WalletContextManager.runWith(issuerContext) {
        DidService.listDids().forEach({ ContextManager.hkvStore.delete(HKVKey("did", "created", it)) })
        val key =
          KeyService.getService().listKeys().firstOrNull { k -> k.algorithm == KeyAlgorithm.ECDSA_Secp256k1 }?.keyId
            ?: KeyService.getService().generate(KeyAlgorithm.ECDSA_Secp256k1)
        val did = DidService.create(DidMethod.ebsi, key.id)
        EssifClient.onboard(did, token)
        EssifClient.authApi(did)
        DidEbsiService.getService().registerDid(did, did)
        println("Issuer DID created and registered: $did")
      }
    } else {
      WalletContextManager.runWith(issuerContext) {
        DidService.listDids().forEach({ ContextManager.hkvStore.delete(HKVKey("did", "created", it)) })
        val did = DidService.create(DidMethod.key)
        println("Issuer DID created: $did")
      }
    }
  }

  fun newNonce(): NonceResponse {
    val nonce = UUID.randomUUID().toString()
    nonceCache.put(nonce, true)
    return NonceResponse(nonce, expires_in = EXPIRATION_TIME.seconds.toString())
  }

  fun getValidNonces(): Set<String> {
    return nonceCache.asMap().keys
  }

  fun newIssuanceInitiationRequest(user: String, selectedIssuables: Issuables, preAuthorized: Boolean, userPin: String? = null): IssuanceInitiationRequest {
    val issuerUri = URI.create("${IssuerConfig.config.issuerApiUrl}/oidc/")
    val session = initializeIssuanceSession(
      credentialDetails = selectedIssuables.credentials.map {issuable ->
        CredentialAuthorizationDetails(issuable.type)
      },
      preAuthorized = preAuthorized,
      authRequest = null,
      userPin = userPin
    )
    updateIssuanceSession(session, selectedIssuables)

    return IssuanceInitiationRequest(
      issuer_url = issuerUri.toString(),
      credential_types = selectedIssuables.credentials.map { it.type },
      pre_authorized_code = if(preAuthorized) generateAuthorizationCodeFor(session) else null,
      user_pin_required = userPin != null,
      op_state = if(!preAuthorized) session.id else null
      )
  }

  fun initializeIssuanceSession(credentialDetails: List<CredentialAuthorizationDetails>, preAuthorized: Boolean, authRequest: AuthorizationRequest?, userPin: String? = null): IssuanceSession {
    val id = UUID.randomUUID().toString()
    //TODO: validata/verify PAR request, claims, etc
    val session = IssuanceSession(
      id,
      credentialDetails,
      UUID.randomUUID().toString(),
      isPreAuthorized = preAuthorized,
      authRequest,
      Issuables.fromCredentialAuthorizationDetails(credentialDetails),
      userPin = userPin
    )
    sessionCache.put(id, session)
    return session
  }

  fun getIssuanceSession(id: String): IssuanceSession? {
    return sessionCache.getIfPresent(id)
  }

  fun updateIssuanceSession(session: IssuanceSession, issuables: Issuables?) {
    session.issuables = issuables
    sessionCache.put(session.id, session)
  }

  fun generateAuthorizationCodeFor(session: IssuanceSession): String {
    return authCodeProvider.generateToken(session)
  }

  fun validateAuthorizationCode(code: String): String {
    return authCodeProvider.validateToken(code).map { it.subject }.orElseThrow { BadRequestResponse("Invalid authorization code given") }
  }

  fun fulfillIssuanceSession(session: IssuanceSession, credentialRequest: CredentialRequest): String? {
    val proof = credentialRequest.proof ?: throw BadRequestResponse("No proof given")
    val parsedJwt = SignedJWT.parse(proof.jwt)
    if(parsedJwt.header.keyID?.let { DidUrl.isDidUrl(it) } == false) throw BadRequestResponse("Proof is not DID signed")
    if(!JwtService.getService().verify(proof.jwt)) throw BadRequestResponse("Proof invalid")

    val did = DidUrl.from(parsedJwt.header.keyID).did

    return WalletContextManager.runWith(issuerContext) {
      session.issuables!!.credentialsByType[credentialRequest.type]?.let {
        Signatory.getService().issue(it.type,
          ProofConfig(
            issuerDid = issuerDid,
            proofType = when (credentialRequest.format) {
              "jwt_vc" -> ProofType.JWT
              else -> ProofType.LD_PROOF
            },
            subjectDid = did
          ),
          dataProvider = it.credentialData?.let { cd -> MergingDataProvider(cd) })
      }
    }
  }

  fun getXDeviceWallet() : WalletConfiguration {
    return WalletConfiguration(
      id = "x-device",
      url = "openid:",
      presentPath = "",
      receivePath = "",
      description = "cross device"
    )
  }
}
