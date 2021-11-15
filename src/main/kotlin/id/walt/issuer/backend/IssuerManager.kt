package id.walt.issuer.backend

import com.google.common.cache.CacheBuilder
import id.walt.auditor.Auditor
import id.walt.auditor.SignaturePolicy
import id.walt.model.DidMethod
import id.walt.model.siopv2.*
import id.walt.services.did.DidService
import id.walt.services.hkvstore.FileSystemHKVStore
import id.walt.services.hkvstore.FilesystemStoreConfig
import id.walt.services.keystore.HKVKeyStoreService
import id.walt.services.vc.VcUtils
import id.walt.services.vcstore.HKVVcStoreService
import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.signatory.Signatory
import id.walt.vclib.Helpers.encode
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.vclist.VerifiablePresentation
import id.walt.verifier.backend.SIOPv2RequestManager
import id.walt.verifier.backend.VerifierConfig
import id.walt.webwallet.backend.context.UserContext
import id.walt.webwallet.backend.context.WalletContextManager
import java.net.URL
import java.nio.file.Path
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

object IssuerManager {

  val issuerContext = UserContext(
    hkvStore = FileSystemHKVStore(FilesystemStoreConfig("data/issuer")),
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
      IssuableCredential("1", "VerifiableId", "Verifiable ID document"),
      IssuableCredential("2", "VerifiableDiploma", "Verifiable diploma"),
    )
  }

  fun newIssuanceRequest(user: String, selectedCredentialIds: Set<String>): SIOPv2Request {
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
    reqCache.put(nonce, IssuanceRequest(user, nonce, selectedCredentialIds))
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
          Signatory.getService().issue(it.type, ProofConfig(issuerDid = issuerDid, proofType = ProofType.LD_PROOF, subjectDid = VcUtils.getSubject(vp_token)))
        }
      } else {
        listOf()
      }
    }
  }
}