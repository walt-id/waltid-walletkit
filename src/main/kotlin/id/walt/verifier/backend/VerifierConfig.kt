package id.walt.verifier.backend

import com.beust.klaxon.Klaxon
import java.io.File

data class VerifierConfig(
  val verifierUiUrl: String = "http://localhost:4000",
  val verifierApiUrl: String = "http://localhost:8080/verifier-api",
  val wallets: Map<String, WalletConfiguration> = WalletConfiguration.getDefaultWalletConfigurations()
) {
  companion object {
    val CONFIG_FILE = "${id.walt.WALTID_DATA_ROOT}/config/verifier-config.json"
    lateinit var config: VerifierConfig
    init {
      val cf = File(CONFIG_FILE)
      if(cf.exists()) {
        config = Klaxon().parse<VerifierConfig>(cf) ?: VerifierConfig()
      } else {
        config = VerifierConfig()
      }
    }
  }
}