package id.walt.issuer.backend

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.nimbusds.oauth2.sdk.GrantType
import com.nimbusds.oauth2.sdk.PreAuthorizedCodeGrant
import com.nimbusds.oauth2.sdk.id.Issuer
import com.nimbusds.openid.connect.sdk.SubjectType
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata
import id.walt.credentials.w3c.W3CIssuer
import id.walt.credentials.w3c.templates.VcTemplateManager
import id.walt.crypto.LdSignatureType
import id.walt.model.DidMethod
import id.walt.model.DidUrl
import id.walt.model.oidc.*
import id.walt.multitenancy.*
import id.walt.oid4vc.data.CredentialFormat
import id.walt.oid4vc.data.CredentialSupported
import id.walt.oid4vc.definitions.JWTClaims
import id.walt.oid4vc.errors.CredentialError
import id.walt.oid4vc.interfaces.CredentialResult
import id.walt.oid4vc.providers.CredentialIssuerConfig
import id.walt.oid4vc.providers.IssuanceSession
import id.walt.oid4vc.providers.OpenIDCredentialIssuer
import id.walt.oid4vc.providers.TokenTarget
import id.walt.oid4vc.requests.CredentialRequest
import id.walt.oid4vc.responses.CredentialErrorCode
import id.walt.services.did.DidService
import id.walt.services.jwt.JwtService
import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.signatory.Signatory
import id.walt.signatory.dataproviders.MergingDataProvider
import id.walt.verifier.backend.WalletConfiguration
import io.github.pavleprica.kotlin.cache.time.based.shortTimeBasedCache
import io.javalin.http.BadRequestResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import java.net.URI
import java.time.Instant
import java.util.*
import kotlin.js.ExperimentalJsExport

const val URL_PATTERN = "^https?:\\/\\/(?!-.)[^\\s\\/\$.?#].[^\\s]*\$"
fun isSchema(typeOrSchema: String): Boolean {
    return Regex(URL_PATTERN).matches(typeOrSchema)
}

object IssuerManager: OpenIDCredentialIssuer(
    IssuerTenant.config.issuerApiUrl + "/oidc/",
    CredentialIssuerConfig(IssuerTenant.config.credentialTypes?.map { CredentialSupported(id.walt.oid4vc.data.CredentialFormat.jwt_vc_json, it) } ?: listOf())
) {
    val log = KotlinLogging.logger { }
    val defaultDid: String
        get() = IssuerTenant.config.issuerDid
            ?: IssuerTenant.state.defaultDid
            ?: DidService.create(DidMethod.key)
                .also {
                    IssuerTenant.state.defaultDid = it
                    log.warn { "No issuer DID configured, created temporary did:key for issuing: $it" }
                }

    fun getIssuerContext(tenantId: String): TenantContext<IssuerConfig, IssuerState> {
        return TenantContextManager.getTenantContext(TenantId(TenantType.ISSUER, tenantId)) { IssuerState() }
    }

    fun getDefaultCredentialTypes() = listOf(
        "VerifiableId",
        "VerifiableDiploma",
        "VerifiableVaccinationCertificate",
        "ProofOfResidence",
        "ParticipantCredential",
        "Europass",
        "OpenBadgeCredential"
    )

    fun listIssuableCredentials(): Issuables {
        return Issuables(
            credentials = (IssuerTenant.config.credentialTypes ?: getDefaultCredentialTypes())
                .map { IssuableCredential.fromTemplateId(it) }
        )
    }

    private fun prompt(prompt: String, default: String?): String? {
        print("$prompt [$default]: ")
        val input = readlnOrNull()
        return when (input.isNullOrBlank()) {
            true -> default
            else -> input
        }
    }

    fun getValidNonces(): Set<String> {
        return IssuerTenant.state.nonceCache.asMap().keys
    }

    private inline fun <T, R> Iterable<T>.allUniqueBy(transform: (T) -> R) =
        HashSet<R>().let { hs ->
            all { hs.add(transform(it)) }
        }


    fun getXDeviceWallet(): WalletConfiguration {
        return WalletConfiguration(
            id = "x-device",
            url = "openid-initiate-issuance://",
            presentPath = "",
            receivePath = "",
            description = "cross device"
        )
    }

    private fun doGenerateCredential(credentialRequest: CredentialRequest): CredentialResult {
        if(credentialRequest.format == CredentialFormat.mso_mdoc) throw CredentialError(credentialRequest, CredentialErrorCode.unsupported_credential_format)
        val types = credentialRequest.types ?: credentialRequest.credentialDefinition?.types ?: throw CredentialError(credentialRequest, CredentialErrorCode.unsupported_credential_type)
        val proofHeader = credentialRequest.proof?.jwt?.let { parseTokenHeader(it) } ?: throw CredentialError(credentialRequest, CredentialErrorCode.invalid_or_missing_proof, message = "Proof must be JWT proof")
        val holderKid = proofHeader[JWTClaims.Header.keyID]?.jsonPrimitive?.content ?: throw CredentialError(credentialRequest, CredentialErrorCode.invalid_or_missing_proof, message = "Proof JWT header must contain kid claim")
        return Signatory.getService().issue(
            types.last(),
            ProofConfig(defaultDid, subjectDid = resolveDIDFor(holderKid)),
            issuer = W3CIssuer(baseUrl),
            storeCredential = false).let {
            when(credentialRequest.format) {
                CredentialFormat.ldp_vc -> Json.decodeFromString<JsonObject>(it)
                else -> JsonPrimitive(it)
            }
        }.let { CredentialResult(credentialRequest.format, it) }
    }

    private fun resolveDIDFor(keyId: String): String {
        return DidUrl.from(keyId).did
    }

    override fun generateCredential(credentialRequest: CredentialRequest): CredentialResult {
        return doGenerateCredential(credentialRequest)
    }

    override fun getDeferredCredential(credentialID: String): CredentialResult {
        TODO("Not yet implemented")
    }

    override fun getSession(id: String): IssuanceSession? {
        return IssuerTenant.state.sessionCache.getIfPresent(id)
    }

    override fun putSession(id: String, session: IssuanceSession): IssuanceSession? {
        IssuerTenant.state.sessionCache.put(id, session)
        return session
    }

    override fun removeSession(id: String): IssuanceSession? {
        val prevVal = IssuerTenant.state.sessionCache.getIfPresent(id)
        IssuerTenant.state.sessionCache.invalidate(id)
        return prevVal
    }

    override fun signToken(target: TokenTarget, payload: JsonObject, header: JsonObject?, keyId: String?): String {
        return JwtService.getService().sign(keyId!!, payload.toString())
    }

    override fun verifyTokenSignature(target: TokenTarget, token: String): Boolean {
        return JwtService.getService().verify(token).verified
    }
}
