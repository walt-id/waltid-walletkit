package id.walt.issuer.backend

import com.google.common.cache.CacheBuilder
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.nimbusds.oauth2.sdk.ResponseMode
import com.nimbusds.oauth2.sdk.id.State
import com.nimbusds.openid.connect.sdk.Nonce
import id.walt.crypto.KeyAlgorithm
import id.walt.model.DidMethod
import id.walt.model.dif.PresentationDefinition
import id.walt.model.oidc.CredentialClaim
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
import id.walt.services.jwt.JwtService
import id.walt.services.oidc.OIDC4VPService
import id.walt.verifier.backend.WalletConfiguration
import id.walt.webwallet.backend.context.UserContext
import id.walt.webwallet.backend.context.WalletContextManager
import io.javalin.http.BadRequestResponse
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
  val reqCache =
    CacheBuilder.newBuilder().expireAfterWrite(EXPIRATION_TIME.seconds, TimeUnit.SECONDS).build<String, IssuanceRequest>()
  val nonceCache =
    CacheBuilder.newBuilder().expireAfterWrite(EXPIRATION_TIME.seconds, TimeUnit.SECONDS).build<String, Boolean>()
  val sessionCache =
    CacheBuilder.newBuilder().expireAfterAccess(EXPIRATION_TIME.seconds, TimeUnit.SECONDS).build<String, IssuanceSession>()
  lateinit var issuerDid: String;

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

  fun newSIOPIssuanceRequest(user: String, selectedIssuables: Issuables, walletUrl: URI): AuthorizationRequest {
    val nonce = UUID.randomUUID().toString()
    val redirectUri = URI.create("${IssuerConfig.config.issuerApiUrl}/credentials/issuance/fulfill")
    val req = OIDC4VPService.createOIDC4VPRequest(
      walletUrl,
      redirect_uri = redirectUri,
      nonce = Nonce(nonce),
      response_mode = ResponseMode("post"),
      presentation_definition = PresentationDefinition(id = "1", listOf()),
      state = State(nonce)
    )
    reqCache.put(nonce, IssuanceRequest(user, nonce, selectedIssuables))
    return req
  }

  fun fulfillIssuanceRequest(nonce: String, vp_token: VerifiablePresentation): List<String> {
    val issuanceReq = reqCache.getIfPresent(nonce);
    if (issuanceReq == null) {
      return listOf()
    }

    return WalletContextManager.runWith(issuerContext) {
      if (vp_token.challenge == nonce &&
        Auditor.getService().verify(vp_token, listOf(SignaturePolicy())).valid
      ) {
        issuanceReq.selectedIssuables.credentials.map {
          Signatory.getService().issue(it.type,
            ProofConfig(
              issuerDid = issuerDid,
              proofType = ProofType.LD_PROOF,
              subjectDid = vp_token.subject
            ),
            dataProvider = it.credentialData?.let { cd -> MergingDataProvider(cd) })
        }
      } else {
        listOf()
      }
    }
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

  fun initializeIssuanceSession(credentialDetails: List<CredentialAuthorizationDetails>, authRequest: AuthorizationRequest): IssuanceSession {
    val id = UUID.randomUUID().toString()
    //TODO: validata/verify PAR request, claims, etc
    val session = IssuanceSession(
      id,
      credentialDetails,
      authRequest,
      UUID.randomUUID().toString(),
      Issuables.fromCredentialAuthorizationDetails(credentialDetails)
    )
    sessionCache.put(id, session)
    return session
  }

  fun getIssuanceSession(id: String): IssuanceSession? {
    return sessionCache.getIfPresent(id)
  }

  fun updateIssuanceSession(session: IssuanceSession, issuables: Issuables?): String {
    session.issuables = issuables
    sessionCache.put(session.id, session)
    return session.id
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
