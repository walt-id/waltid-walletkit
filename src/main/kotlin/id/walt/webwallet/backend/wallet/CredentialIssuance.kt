package id.walt.webwallet.backend.wallet

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.nimbusds.openid.connect.sdk.token.OIDCTokens
import id.walt.custodian.Custodian
import id.walt.model.DidMethod
import id.walt.model.DidUrl
import id.walt.model.oidc.*
import id.walt.services.context.ContextManager
import id.walt.services.oidc.OIDC4CIService
import id.walt.vclib.model.VerifiableCredential
import id.walt.webwallet.backend.auth.UserInfo
import id.walt.webwallet.backend.config.WalletConfig
import id.walt.webwallet.backend.context.WalletContextManager
import io.javalin.http.BadRequestResponse
import io.javalin.http.InternalServerErrorResponse
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.*

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
data class CredentialIssuanceRequest(
    val did: String,
    val issuerId: String,
    val credentialTypes: List<String>,
    val walletRedirectUri: String,
)

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
data class CredentialIssuanceSession(
    val id: String,
    val issuerId: String,
    val credentialTypes: List<String>,
    val isPreAuthorized: Boolean,
    val isIssuerInitiated: Boolean,
    val userPinRequired: Boolean,
    @JsonIgnore val nonce: String,
    var did: String? = null,
    var walletRedirectUri: String? = null,
    @JsonIgnore var user: UserInfo? = null,
    @JsonIgnore var tokens: OIDCTokens? = null,
    @JsonIgnore var lastTokenUpdate: Instant? = null,
    @JsonIgnore var tokenNonce: String? = null,
    @JsonIgnore var preAuthzCode: String? = null,
    @JsonIgnore var opState: String? = null,
    var credentials: List<VerifiableCredential>? = null
) {
    companion object {
        fun fromIssuanceRequest(credentialIssuanceRequest: CredentialIssuanceRequest): CredentialIssuanceSession {
            return CredentialIssuanceSession(
                id = UUID.randomUUID().toString(),
                issuerId = credentialIssuanceRequest.issuerId,
                credentialTypes = credentialIssuanceRequest.credentialTypes,
                false, false, false,
                nonce = UUID.randomUUID().toString(),
                did = credentialIssuanceRequest.did,
                walletRedirectUri = credentialIssuanceRequest.walletRedirectUri
            )
        }

        fun fromInitiationRequest(issuanceInitiationRequest: IssuanceInitiationRequest): CredentialIssuanceSession {
            return CredentialIssuanceSession(
                id = UUID.randomUUID().toString(),
                issuerId = issuanceInitiationRequest.issuer_url,
                credentialTypes = issuanceInitiationRequest.credential_types,
                isPreAuthorized = issuanceInitiationRequest.isPreAuthorized,
                isIssuerInitiated = true,
                userPinRequired = issuanceInitiationRequest.user_pin_required,
                nonce = UUID.randomUUID().toString()
            )
        }
    }
}

object CredentialIssuanceManager {
    val EXPIRATION_TIME = Duration.ofMinutes(5)
    val sessionCache = CacheBuilder.newBuilder().expireAfterAccess(EXPIRATION_TIME.seconds, TimeUnit.SECONDS)
        .build<String, CredentialIssuanceSession>()
    val issuerCache: LoadingCache<String, OIDCProviderWithMetadata> = CacheBuilder.newBuilder().maximumSize(256)
        .build(
            CacheLoader.from { issuerId ->
                (   // find issuer from config
                    WalletConfig.config.issuers[issuerId!!] ?:
                    // else, assume issuerId is a valid issuer url
                    OIDCProvider(issuerId, issuerId)
                ).let {
                    OIDC4CIService.getWithProviderMetadata(it)
                }
            }
    )

    val redirectURI: URI
        get() = URI.create("${WalletConfig.config.walletApiUrl}/wallet/siopv2/finalizeIssuance")

    private fun getPreferredFormat(credentialTypeId: String, did: String, supportedCredentials: Map<String, CredentialMetadata>): String? {
        val preferredByEcosystem = when(DidUrl.from(did).method) {
            DidMethod.iota.name -> "ldp_vc"
            DidMethod.ebsi.name -> "jwt_vc"
            else -> "jwt_vc"
        }
        if(supportedCredentials.containsKey(credentialTypeId)) {
            if(supportedCredentials[credentialTypeId]!!.formats.containsKey(preferredByEcosystem) == false) {
                // ecosystem preference is explicitly not supported, check if ldp_vc or jwt_vc is
                return supportedCredentials[credentialTypeId]!!.formats.keys.firstOrNull { fmt -> setOf("jwt_vc", "ldp_vc").contains(fmt) }
            }
        }
        return preferredByEcosystem
    }

    private fun executeAuthorizationStep(session: CredentialIssuanceSession): URI {
        val issuer = issuerCache[session.issuerId]

        val supportedCredentials = OIDC4CIService.getSupportedCredentials(issuer)
        val credentialDetails = session.credentialTypes.map {
            CredentialAuthorizationDetails(
                credential_type = it,
                format = getPreferredFormat(it, session.did!!, supportedCredentials)
            )
        }

        return OIDC4CIService.executePushedAuthorizationRequest(
            issuer,
            redirectURI,
            credentialDetails,
            nonce = session.nonce,
            state = session.id,
            wallet_issuer = WalletConfig.config.walletApiUrl,
            user_hint = URI.create(WalletConfig.config.walletUiUrl).authority,
            op_state = session.opState
        ) ?: throw InternalServerErrorResponse("Could not execute pushed authorization request on issuer")
    }

    fun initIssuance(issuanceRequest: CredentialIssuanceRequest, user: UserInfo): URI {

        val session = CredentialIssuanceSession.fromIssuanceRequest(issuanceRequest).apply {
            this.user = user
        }
        return executeAuthorizationStep(session).also {
            putSession(session)
        }
    }

    fun startIssuerInitiatedIssuance(issuanceInitiationRequest: IssuanceInitiationRequest): String {
        val session = CredentialIssuanceSession.fromInitiationRequest(issuanceInitiationRequest)
        putSession(session)
        return session.id
    }

    fun continueIssuerInitiatedIssuance(sessionId: String, did: String, user: UserInfo): URI? {
        val session = sessionCache.getIfPresent(sessionId) ?: throw BadRequestResponse("Session invalid or not found")
        if(!session.isIssuerInitiated) throw BadRequestResponse("Session is not issuer initiated")
        session.did = did
        session.user = user
        putSession(session)
        if(!session.isPreAuthorized) {
            return executeAuthorizationStep(session)
        }
        return null
    }

    private fun enc(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)

    fun finalizeIssuance(id: String, code: String, userPin: String? = null): CredentialIssuanceSession? {
        val session = sessionCache.getIfPresent(id) ?: return null
        val issuer = issuerCache[session.issuerId]
        val user = session.user ?: throw BadRequestResponse("Session has not been confirmed by user")
        val did = session.did ?: throw BadRequestResponse("No DID assigned to session")

        val tokenResponse = OIDC4CIService.getAccessToken(issuer, code, redirectURI.toString(), session.isPreAuthorized, userPin)
        if (!tokenResponse.indicatesSuccess()) {
            return session
        }
        session.tokens = tokenResponse.toSuccessResponse().oidcTokens
        session.lastTokenUpdate = Instant.now()
        tokenResponse.customParameters["c_nonce"]?.toString()?.also {
            session.tokenNonce = it
        }

        ContextManager.runWith(WalletContextManager.getUserContext(user)) {
            session.credentials = session.credentialTypes.map { typeId ->
                OIDC4CIService.getCredential(
                    issuer,
                    session.tokens!!.accessToken,
                    typeId,
                    OIDC4CIService.generateDidProof(issuer, did, session.tokenNonce ?: "")
                )
            }.filterNotNull().map { it }

            session.credentials?.forEach {
                it.id = it.id ?: UUID.randomUUID().toString()
                Custodian.getService().storeCredential(it.id!!, it)
            }
        }

        return session
    }

    fun getSession(id: String): CredentialIssuanceSession? {
        return sessionCache.getIfPresent(id)
    }

    fun putSession(session: CredentialIssuanceSession) {
        sessionCache.put(session.id, session)
    }

    fun findIssuersFor(requiredSchemaIds: Set<String>): List<OIDCProvider> {
        return WalletConfig.config.issuers.keys.map { issuerCache[it] }.filter { issuer ->
            OIDC4CIService.getSupportedCredentials(issuer)
                .flatMap { manifest -> manifest.outputDescriptors.map { outDesc -> outDesc.schema } }
                ?.toSet()
                ?.containsAll(requiredSchemaIds) ?: false
        }.map {
            OIDCProvider(it.id, it.url, it.description) // strip secrets
        }
    }
}
