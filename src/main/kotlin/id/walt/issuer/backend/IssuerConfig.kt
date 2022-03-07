package id.walt.issuer.backend

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import id.walt.verifier.backend.WalletConfiguration
import id.walt.webwallet.backend.config.ExternalHostnameUrl
import id.walt.webwallet.backend.config.externalHostnameUrlValueConverter
import java.io.File

data class IssuerConfig(
  @ExternalHostnameUrl val issuerUiUrl: String = "http://localhost:5000",
  @ExternalHostnameUrl val issuerApiUrl: String = "http://localhost:8080/issuer-api",
  @Json(serializeNull = false) val issuerClientName: String = "Walt.id Issuer Portal",
  val wallets: Map<String, WalletConfiguration> = WalletConfiguration.getDefaultWalletConfigurations(),
  val issuerDid: String? = null
) {
  val onboardingApiUrl
    get() = issuerApiUrl.replace("/issuer-api", "/onboarding-api")
  val onboardingUiUrl
    get() = "$issuerUiUrl/Onboarding/"

  companion object {
    val CONFIG_FILE = "${id.walt.WALTID_DATA_ROOT}/config/issuer-config.json"
    lateinit var config: IssuerConfig
    init {
      val cf = File(CONFIG_FILE)
      if(cf.exists()) {
        config = Klaxon().fieldConverter(ExternalHostnameUrl::class, externalHostnameUrlValueConverter).parse<IssuerConfig>(cf) ?: IssuerConfig()
      } else {
        config = IssuerConfig()
      }
    }
  }
}
