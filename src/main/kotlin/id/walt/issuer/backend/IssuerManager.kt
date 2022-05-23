package id.walt.issuer.backend

import com.google.common.cache.CacheBuilder
import com.nimbusds.oauth2.sdk.AuthorizationRequest
import id.walt.crypto.KeyAlgorithm
import id.walt.model.DidMethod
import id.walt.model.dif.PresentationDefinition
import id.walt.model.oidc.*
import id.walt.services.context.ContextManager
import id.walt.services.did.DidService
import id.walt.services.essif.EssifClient
import id.walt.services.essif.didebsi.DidEbsiService
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
import id.walt.webwallet.backend.context.UserContext
import id.walt.webwallet.backend.context.WalletContextManager
import java.net.URI
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

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
  val reqCache = CacheBuilder.newBuilder().expireAfterWrite(EXPIRATION_TIME.seconds, TimeUnit.SECONDS).build<String, IssuanceRequest>()
  val nonceCache = CacheBuilder.newBuilder().expireAfterWrite(EXPIRATION_TIME.seconds, TimeUnit.SECONDS).build<String, Boolean>()
  val sessionCache = CacheBuilder.newBuilder().expireAfterAccess(EXPIRATION_TIME.seconds, TimeUnit.SECONDS).build<String, IssuanceSession>()
  lateinit var issuerDid: String;

  init {
    WalletContextManager.runWith(issuerContext) {
      issuerDid = IssuerConfig.config.issuerDid ?: DidService.listDids().firstOrNull() ?: DidService.create(DidMethod.key)
    }
  }

  fun listIssuableCredentialsFor(user: String): Issuables {
    return Issuables(
      credentials = listOf("VerifiableId", "VerifiableDiploma", "VerifiableVaccinationCertificate", "ProofOfResidence", "ParticipantCredential", "Europass")
        .map { IssuableCredential.fromTemplateId(it) }
    )
  }

  fun newSIOPIssuanceRequest(user: String, selectedIssuables: Issuables): SIOPv2Request {
    val nonce = UUID.randomUUID().toString()
    val redirectUri = URI.create("${IssuerConfig.config.issuerApiUrl}/credentials/issuance/fulfill")
    val req = SIOPv2Request(
      redirect_uri = redirectUri.toString(),
      response_mode = "post",
      nonce = nonce,
      claims = VCClaims(vp_token = VpTokenClaim(PresentationDefinition(id = "1", listOf()))),
      state = nonce
    )
    reqCache.put(nonce, IssuanceRequest(user, nonce, selectedIssuables))
    return req
  }

  fun fulfillIssuanceRequest(nonce: String, id_token: IDToken?, vp_token: VerifiablePresentation): List<String> {
    val issuanceReq = reqCache.getIfPresent(nonce);
    if(issuanceReq == null) {
      return listOf()
    }
    // TODO: verify id_token!!
    return WalletContextManager.runWith(issuerContext) {
      if(vp_token.challenge == nonce &&
        // TODO: verify id_token subject
        //id_token.subject == VcUtils.getSubject(vp_token) &&
        // TODO: verify VP signature (import public key for did, currently not supported for did:key!)
        //Auditor.getService().verify(vp_token.encode(), listOf(SignaturePolicy())).overallStatus
        true
      ) {
        issuanceReq.selectedIssuables.credentials.map {
          Signatory.getService().issue(it.type,
            ProofConfig(issuerDid = issuerDid,
              proofType = ProofType.LD_PROOF,
              subjectDid = vp_token.subject),
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
    return when(input.isNullOrBlank()) {
      true -> default
      else -> input
    }
  }

  fun initializeInteractively() {
    val method = prompt("DID method ('key' or 'ebsi') [key]", "key")
    if(method == "ebsi") {
      val token = prompt("EBSI bearer token: ", null)
      if(token.isNullOrEmpty()) {
        println("EBSI bearer token required, to register EBSI did")
        return
      }
      WalletContextManager.runWith(issuerContext) {
        DidService.listDids().forEach( { ContextManager.hkvStore.delete(HKVKey("did", "created", it)) })
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
        DidService.listDids().forEach( { ContextManager.hkvStore.delete(HKVKey("did", "created", it)) })
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

  fun initializeIssuanceSession(credentialClaims: List<CredentialClaim>, authRequest: AuthorizationRequest): IssuanceSession {
    val id = UUID.randomUUID().toString()
    //TODO: validata/verify PAR request, VP tokens, claims, etc
    val session = IssuanceSession(id, credentialClaims, authRequest, UUID.randomUUID().toString(), Issuables.fromCredentialClaims(credentialClaims))
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

  fun fulfillIssuanceSession(session: IssuanceSession, typeOrSchema: String, did: String, format: String = "ldp_vc"): String? {
    return WalletContextManager.runWith(issuerContext) {
      when(isSchema(typeOrSchema)) {
        true -> session.issuables!!.credentialsBySchemaId[typeOrSchema]
        else -> session.issuables!!.credentialsByType[typeOrSchema]
      }?.let {
          Signatory.getService().issue(it.type,
            ProofConfig(
              issuerDid = issuerDid,
              proofType = when (format) {
                "jwt" -> ProofType.JWT
                else -> ProofType.LD_PROOF
              },
              subjectDid = did
            ),
            dataProvider = it.credentialData?.let { cd -> MergingDataProvider(cd) })
        }
    }
  }
}
