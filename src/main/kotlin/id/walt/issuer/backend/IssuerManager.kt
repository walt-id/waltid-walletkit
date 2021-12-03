package id.walt.issuer.backend

import com.google.common.cache.CacheBuilder
import id.walt.auditor.Auditor
import id.walt.auditor.SignaturePolicy
import id.walt.crypto.KeyAlgorithm
import id.walt.model.DidMethod
import id.walt.model.siopv2.*
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
import id.walt.vclib.Helpers.encode
import id.walt.vclib.VcUtils
import id.walt.vclib.credentials.VerifiablePresentation
import id.walt.vclib.model.VerifiableCredential
import id.walt.verifier.backend.SIOPv2RequestManager
import id.walt.verifier.backend.VerifierConfig
import id.walt.webwallet.backend.WALTID_DATA_ROOT
import id.walt.webwallet.backend.context.UserContext
import id.walt.webwallet.backend.context.WalletContextManager
import java.net.URL
import java.nio.file.Path
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

object IssuerManager {

  val issuerContext = UserContext(
    hkvStore = FileSystemHKVStore(FilesystemStoreConfig("$WALTID_DATA_ROOT/data/issuer")),
    keyStore = HKVKeyStoreService(),
    vcStore = HKVVcStoreService()
  )

  val reqCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build<String, IssuanceRequest>()
  lateinit var issuerDid: String;

  init {
    WalletContextManager.runWith(issuerContext) {
      issuerDid = DidService.listDids().firstOrNull() ?: DidService.create(DidMethod.key)
    }
  }

  fun listIssuableCredentialsFor(user: String): List<IssuableCredential> {
    return listOf(
//      IssuableCredential("1", "VerifiableId", "Verifiable ID document"),
//      IssuableCredential("2", "VerifiableDiploma", "Verifiable diploma"),
      IssuableCredential("1", "VerifiableVaccinationCertificate", "Verifiable vaccination certificate"),
    )
  }

  fun newIssuanceRequest(user: String, selectedCredentialIds: Set<String>, params: Map<String, List<String>>): SIOPv2Request {
    val selectedCredentials = listIssuableCredentialsFor(user).filter { selectedCredentialIds.contains(it.id) }
    val nonce = UUID.randomUUID().toString()
    val req = SIOPv2Request(
      client_id = "${IssuerConfig.config.issuerApiUrl}/credentials/issuance/fulfill/$nonce",
      redirect_uri = "${IssuerConfig.config.issuerApiUrl}/credentials/issuance/fulfill/$nonce",
      response_mode = "post",
      nonce = nonce,
      registration = Registration(client_name = "Walt.id Issuer Portal", client_purpose = "Verify DID ownership, for issuance of ${selectedCredentials.map { it.description }.joinToString(", ") }"),
      expiration = Instant.now().epochSecond + 24*60*60,
      issuedAt = Instant.now().epochSecond,
      claims = Claims()
    )
    reqCache.put(nonce, IssuanceRequest(user, nonce, selectedCredentialIds, params))
    return req
  }

  fun fulfillIssuanceRequest(nonce: String, id_token: SIOPv2IDToken?, vp_token: VerifiablePresentation): List<String> {
    val issuanceReq = reqCache.getIfPresent(nonce);
    if(issuanceReq == null) {
      return listOf()
    }
    // TODO: verify id_token!!
    return WalletContextManager.runWith(issuerContext) {
      if(VcUtils.getChallenge(vp_token) == nonce &&
        // TODO: verify id_token subject
        //id_token.subject == VcUtils.getSubject(vp_token) &&
        // TODO: verify VP signature (import public key for did, currently not supported for did:key!)
        //Auditor.getService().verify(vp_token.encode(), listOf(SignaturePolicy())).overallStatus
        true
      ) {
        val selectedCredentials = listIssuableCredentialsFor(issuanceReq.user).filter { issuanceReq.selectedCredentialIds.contains(it.id) }
        selectedCredentials.map {
          Signatory.getService().issue(it.type,
            ProofConfig(issuerDid = issuerDid,
              proofType = ProofType.LD_PROOF,
              subjectDid = VcUtils.getSubject(vp_token)),
            dataProvider = IssuanceRequestDataProvider(issuanceReq))
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
}