package id.walt.issuer.backend

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.nimbusds.oauth2.sdk.GrantType
import com.nimbusds.oauth2.sdk.PreAuthorizedCodeGrant
import com.nimbusds.oauth2.sdk.id.Issuer
import com.nimbusds.openid.connect.sdk.SubjectType
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata
import id.walt.credentials.w3c.templates.VcTemplateManager
import id.walt.crypto.LdSignatureType
import id.walt.model.DidMethod
import id.walt.model.DidUrl
import id.walt.model.oidc.*
import id.walt.multitenancy.TenantContext
import id.walt.multitenancy.TenantContextManager
import id.walt.multitenancy.TenantId
import id.walt.multitenancy.TenantType
import id.walt.services.did.DidService
import id.walt.services.jwt.JwtService
import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.signatory.Signatory
import id.walt.signatory.dataproviders.MergingDataProvider
import id.walt.verifier.backend.WalletConfiguration
import io.github.pavleprica.kotlin.cache.time.based.shortTimeBasedCache
import io.javalin.http.BadRequestResponse
import mu.KotlinLogging
import java.net.URI
import java.time.Instant
import java.util.*

const val URL_PATTERN = "^https?:\\/\\/(?!-.)[^\\s\\/\$.?#].[^\\s]*\$"
fun isSchema(typeOrSchema: String): Boolean {
    return Regex(URL_PATTERN).matches(typeOrSchema)
}

object IssuerManager {
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

    fun listIssuableCredentials(): Issuables {
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
        val input = readlnOrNull()
        return when (input.isNullOrBlank()) {
            true -> default
            else -> input
        }
    }

    fun getValidNonces(): Set<String> {
        return IssuerTenant.state.nonceCache.asMap().keys
    }

    fun newIssuanceInitiationRequest(
        selectedIssuables: Issuables,
        preAuthorized: Boolean,
        userPin: String? = null,
        issuerDid: String? = null
    ): IssuanceInitiationRequest {
        val issuerUri = URI.create("${IssuerTenant.config.issuerApiUrl}/oidc/")
        val session = initializeIssuanceSession(
            credentialDetails = selectedIssuables.credentials.map { issuable ->
                CredentialAuthorizationDetails(issuable.type)
            },
            preAuthorized = preAuthorized,
            authRequest = null,
            userPin = userPin,
            issuerDid = issuerDid
        )
        updateIssuanceSession(session, selectedIssuables, issuerDid)

        return IssuanceInitiationRequest(
            issuer_url = issuerUri.toString(),
            credential_types = selectedIssuables.credentials.map { it.type },
            pre_authorized_code = if (preAuthorized) generateAuthorizationCodeFor(session) else null,
            user_pin_required = userPin != null,
            op_state = if (!preAuthorized) session.id else null
        )
    }

    fun initializeIssuanceSession(
        credentialDetails: List<CredentialAuthorizationDetails>,
        preAuthorized: Boolean,
        authRequest: AuthorizationRequest?,
        userPin: String? = null,
        issuerDid: String? = null
    ): IssuanceSession {
        val id = UUID.randomUUID().toString()
        //TODO: validata/verify PAR request, claims, etc
        val session = IssuanceSession(
            id,
            credentialDetails,
            UUID.randomUUID().toString(),
            isPreAuthorized = preAuthorized,
            authRequest,
            Issuables.fromCredentialAuthorizationDetails(credentialDetails),
            userPin = userPin,
            issuerDid = issuerDid
        )
        IssuerTenant.state.sessionCache.put(id, session)
        return session
    }

    fun getIssuanceSession(id: String): IssuanceSession? {
        return IssuerTenant.state.sessionCache.getIfPresent(id)
    }

    fun updateIssuanceSession(session: IssuanceSession, issuables: Issuables?, issuerDid: String? = null) {
        session.issuables = issuables
        issuerDid?.let { session.issuerDid = issuerDid }
        IssuerTenant.state.sessionCache.put(session.id, session)
    }

    fun generateAuthorizationCodeFor(session: IssuanceSession): String {
        return IssuerTenant.state.authCodeProvider.generateToken(session)
    }

    fun validateAuthorizationCode(code: String): String {
        return IssuerTenant.state.authCodeProvider.validateToken(code).map { it.subject }
            .orElseThrow { BadRequestResponse("Invalid authorization code given") }
    }

    private inline fun <T, R> Iterable<T>.allUniqueBy(transform: (T) -> R) =
        HashSet<R>().let { hs ->
            all { hs.add(transform(it)) }
        }

    /**
     * For multipleCredentialsOfSameType in session.issuables
     */
    private val sessionAccessCounterCache = shortTimeBasedCache<String, HashMap<String, Int>>()
    fun fulfillIssuanceSession(session: IssuanceSession, credentialRequest: CredentialRequest): String? {
        val proof = credentialRequest.proof ?: throw BadRequestResponse("No proof given")
        val parsedJwt = SignedJWT.parse(proof.jwt)
        if (parsedJwt.header.keyID?.let { DidUrl.isDidUrl(it) } == false) throw BadRequestResponse("Proof is not DID signed")

        if (!JwtService.getService().verify(proof.jwt)) throw BadRequestResponse("Proof invalid")

        val did = DidUrl.from(parsedJwt.header.keyID).did
        val now = Instant.now()
        val issuables = session.issuables ?: throw BadRequestResponse("No issuables")

        log.debug { "Issuance session ${session.id}: Session issuables: ${session.issuables}" }

        val sessionLongId = "${session.id}${session.nonce}"


        val multipleCredentialsOfSameType = !issuables.credentials.allUniqueBy { it.type }

        if (multipleCredentialsOfSameType && sessionAccessCounterCache[sessionLongId].isEmpty) {
            log.debug { "Issuance session ${session.id}: Setup multipleCredentialsOfSameType" }
            sessionAccessCounterCache[sessionLongId] = HashMap()
        }

        val requestedType = credentialRequest.type
        val credentialsOfRequestedType = issuables.credentials.filter { it.type == requestedType }

        val credential = when {
            !multipleCredentialsOfSameType -> credentialsOfRequestedType.firstOrNull()
            else -> {
                val accessCounter = sessionAccessCounterCache[sessionLongId].get()

                if (!accessCounter.contains(requestedType))
                    accessCounter[requestedType] = -1

                accessCounter[requestedType] = accessCounter[requestedType]!! + 1

                log.info {
                    "Issuance session ${session.id}: multipleCredentialsOfSameType " +
                            "request ${accessCounter[requestedType]!! + 1}/${issuables.credentials.size}"
                }

                credentialsOfRequestedType.getOrElse(accessCounter[requestedType]!!) { credentialsOfRequestedType.lastOrNull() }
            }
        }

        return credential?.let {
            Signatory.getService().issue(it.type,
                ProofConfig(
                    issuerDid = session.issuerDid ?: defaultDid,
                    proofType = when (credentialRequest.format) {
                        "jwt_vc" -> ProofType.JWT
                        else -> ProofType.LD_PROOF
                    },
                    subjectDid = did,
                    issueDate = now,
                    validDate = now
                ),
                dataProvider = it.credentialData?.let { cd -> MergingDataProvider(cd) })
        }
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

    fun getOidcProviderMetadata() = OIDCProviderMetadata(
        Issuer(IssuerTenant.config.issuerApiUrl),
        listOf(SubjectType.PUBLIC),
        URI("${IssuerTenant.config.issuerApiUrl}/oidc")
    ).apply {
        authorizationEndpointURI = URI("${IssuerTenant.config.issuerApiUrl}/oidc/fulfillPAR")
        pushedAuthorizationRequestEndpointURI = URI("${IssuerTenant.config.issuerApiUrl}/oidc/par")
        tokenEndpointURI = URI("${IssuerTenant.config.issuerApiUrl}/oidc/token")
        grantTypes = listOf(GrantType.AUTHORIZATION_CODE, PreAuthorizedCodeGrant.GRANT_TYPE)
        setCustomParameter("credential_endpoint", "${IssuerTenant.config.issuerApiUrl}/oidc/credential")
        setCustomParameter(
            "credential_issuer", CredentialIssuer(
                listOf(
                    CredentialIssuerDisplay(IssuerTenant.config.issuerApiUrl)
                )
            )
        )
        setCustomParameter(
            "credentials_supported",
            VcTemplateManager.listTemplates().map { VcTemplateManager.getTemplate(it.name, true) }
                .associateBy({ tmpl -> tmpl.template!!.type.last() }) { cred ->
                    CredentialMetadata(
                        formats = mapOf(
                            "ldp_vc" to CredentialFormat(
                                types = cred.template!!.type,
                                cryptographic_binding_methods_supported = listOf("did"),
                                cryptographic_suites_supported = LdSignatureType.values().map { it.name }
                            ),
                            "jwt_vc" to CredentialFormat(
                                types = cred.template!!.type,
                                cryptographic_binding_methods_supported = listOf("did"),
                                cryptographic_suites_supported = listOf(
                                    JWSAlgorithm.ES256,
                                    JWSAlgorithm.ES256K,
                                    JWSAlgorithm.EdDSA,
                                    JWSAlgorithm.RS256,
                                    JWSAlgorithm.PS256
                                ).map { it.name }
                            )
                        ),
                        display = listOf(
                            CredentialDisplay(
                                name = cred.template!!.type.last()
                            )
                        )
                    )
                }
        )
    }
}
