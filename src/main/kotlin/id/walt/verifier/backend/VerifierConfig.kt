package id.walt.verifier.backend

import com.beust.klaxon.Klaxon
import id.walt.auditor.PolicyRequest
import id.walt.issuer.backend.IssuerConfig
import id.walt.multitenancy.TenantConfig
import id.walt.multitenancy.TenantConfigFactory
import id.walt.webwallet.backend.config.ExternalHostnameUrl
import id.walt.webwallet.backend.config.externalHostnameUrlValueConverter
import java.io.File

data class VerifierConfig(
    @ExternalHostnameUrl val verifierUiUrl: String = "http://localhost:4000",
    @ExternalHostnameUrl val verifierApiUrl: String = "http://localhost:8080/verifier-api",
    val wallets: Map<String, WalletConfiguration> = WalletConfiguration.getDefaultWalletConfigurations(),
    val additionalPolicies: List<PolicyRequest>? = null,
    val allowedWebhookHosts: List<String>? = null
): TenantConfig {
    companion object: TenantConfigFactory<VerifierConfig> {
        val CONFIG_FILE = "${id.walt.WALTID_DATA_ROOT}/config/verifier-config.json"

        override fun fromJson(json: String): VerifierConfig {
            return Klaxon().fieldConverter(ExternalHostnameUrl::class, externalHostnameUrlValueConverter)
                .parse(json) ?: VerifierConfig()
        }
        override fun forDefaultTenant(): VerifierConfig {
            val cf = File(CONFIG_FILE)
            return if(cf.exists()) {
                fromJson(cf.readText())
            } else {
                VerifierConfig()
            }
        }
    }

    override fun toJson(): String {
        return Klaxon().fieldConverter(ExternalHostnameUrl::class, externalHostnameUrlValueConverter).toJsonString(this)
    }
}
