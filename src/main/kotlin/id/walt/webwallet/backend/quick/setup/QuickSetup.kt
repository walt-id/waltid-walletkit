package id.walt.webwallet.backend.quick.setup

import id.walt.crypto.KeyAlgorithm
import id.walt.issuer.backend.IssuerConfig
import id.walt.issuer.backend.IssuerManager
import id.walt.issuer.backend.IssuerTenant
import id.walt.model.DidMethod
import id.walt.multitenancy.TenantType
import id.walt.services.did.DidService
import id.walt.services.key.KeyService
import id.walt.verifier.backend.VerifierConfig
import id.walt.verifier.backend.VerifierManager
import id.walt.verifier.backend.VerifierTenant
import id.walt.webwallet.backend.context.WalletContextManager
import kotlinx.serialization.Serializable
import java.security.SecureRandom
import java.util.*


object QuickSetup {
    private val verifierManager = VerifierManager.getService()
    private val keyService = KeyService.getService()

    fun run(hosts: List<String>) = generateToken(9).let {
        QuickConfig(
            issuer = createConfig("iss-tenant-$it", TenantType.ISSUER),
            verifier = createConfig("vfr-tenant-$it", TenantType.VERIFIER, hosts),
        )
    }

    private fun createConfig(tenantId: String, type: TenantType, hosts: List<String>? = null) = when (type) {
        TenantType.ISSUER -> createIssuerConfig(
            tenantId,
            DidService.create(DidMethod.key, keyService.generate(KeyAlgorithm.EdDSA_Ed25519).id)
        ).let {
            QuickConfig.IssuerTenantQuickConfig(
                tenantId = tenantId,
                url = it.issuerApiUrl,
                did = it.issuerDid ?: "",
            )
        }

        TenantType.VERIFIER -> createVerifierConfig(tenantId, hosts ?: emptyList()).let {
            QuickConfig.VerifierTenantQuickConfig(
                tenantId = tenantId,
                url = it.verifierApiUrl,
                allowedWebhookHosts = it.allowedWebhookHosts ?: emptyList(),
            )
        }
    }

    private fun createIssuerConfig(tenantId: String, did: String) = let {
        // copy default config
        WalletContextManager.setCurrentContext(IssuerManager.getIssuerContext("default"))
        val defaultConfig = IssuerTenant.config
        WalletContextManager.resetCurrentContext()
        // create issuer config
        WalletContextManager.setCurrentContext(IssuerManager.getIssuerContext(tenantId))
        val tenantConfig = IssuerConfig(
            issuerApiUrl = defaultConfig.issuerApiUrl.removeSuffix("/default").plus("/$tenantId"),
            issuerDid = did,
            wallets = defaultConfig.wallets
        )
        IssuerTenant.setConfig(tenantConfig)
        WalletContextManager.resetCurrentContext()
        tenantConfig
    }

    private fun createVerifierConfig(tenantId: String, hosts: List<String>) = let {
        // copy default config
        WalletContextManager.setCurrentContext(verifierManager.getVerifierContext("default"))
        val defaultConfig = VerifierTenant.config
        WalletContextManager.resetCurrentContext()
        // create verifier config
        WalletContextManager.setCurrentContext(verifierManager.getVerifierContext(tenantId))
        val tenantConfig = VerifierConfig(
            verifierApiUrl = defaultConfig.verifierApiUrl.removeSuffix("/default").plus("/$tenantId"),
            allowedWebhookHosts = hosts,
            wallets = defaultConfig.wallets
        )
        VerifierTenant.setConfig(tenantConfig)
        WalletContextManager.resetCurrentContext()
        tenantConfig
    }

    fun generateToken(size: Int): String {
        val random = SecureRandom()
        val bytes = ByteArray(size)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}

@Serializable
data class QuickConfig(
    val issuer: TenantQuickConfig,
    val verifier: TenantQuickConfig,
) {
    @Serializable
    abstract class TenantQuickConfig {
        abstract val tenantId: String
        abstract val url: String
    }

    @Serializable
    data class IssuerTenantQuickConfig(
        override val tenantId: String,
        override val url: String,
        val did: String,
    ) : TenantQuickConfig()

    data class VerifierTenantQuickConfig(
        override val tenantId: String,
        override val url: String,
        val allowedWebhookHosts: List<String>,
    ) : TenantQuickConfig()
}