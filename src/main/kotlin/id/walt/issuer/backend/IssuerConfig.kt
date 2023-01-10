package id.walt.issuer.backend

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import id.walt.multitenancy.TenantConfig
import id.walt.multitenancy.TenantConfigFactory
import id.walt.verifier.backend.WalletConfiguration
import id.walt.webwallet.backend.config.ExternalHostnameUrl
import id.walt.webwallet.backend.config.externalHostnameUrlValueConverter
import java.io.File

data class IssuerConfig(
    @ExternalHostnameUrl val issuerUiUrl: String = "http://localhost:5000",
    @ExternalHostnameUrl val issuerApiUrl: String = "http://localhost:8080/issuer-api/default",
    @Json(serializeNull = false) val issuerClientName: String = "Walt.id Issuer Portal",
    val wallets: Map<String, WalletConfiguration> = WalletConfiguration.getDefaultWalletConfigurations(),
    val issuerDid: String? = null
): TenantConfig {
    @Json(ignored = true)
    val onboardingApiUrl
        get() = issuerApiUrl.replace("/issuer-api", "/onboarding-api")
    @Json(ignored = true)
    val onboardingUiUrl
        get() = "$issuerUiUrl/Onboarding/"

    override fun toJson(): String {
        return Klaxon().fieldConverter(ExternalHostnameUrl::class, externalHostnameUrlValueConverter).toJsonString(this)
    }

    companion object: TenantConfigFactory<IssuerConfig> {

        val CONFIG_FILE = "${id.walt.WALTID_DATA_ROOT}/config/issuer-config.json"

        override fun fromJson(json: String): IssuerConfig {
            return Klaxon().fieldConverter(ExternalHostnameUrl::class, externalHostnameUrlValueConverter)
                .parse(json) ?: IssuerConfig()
        }
        override fun forDefaultTenant(): IssuerConfig {
            val cf = File(CONFIG_FILE)
            return if(cf.exists()) {
                fromJson(cf.readText())
            } else {
                IssuerConfig()
            }
        }
    }
}
