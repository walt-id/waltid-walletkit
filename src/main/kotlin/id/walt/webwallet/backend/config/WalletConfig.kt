package id.walt.webwallet.backend.config

import com.beust.klaxon.Klaxon
import id.walt.issuer.backend.IssuerConfig
import id.walt.model.oidc.OIDCProvider
import id.walt.verifier.backend.WalletConfiguration
import java.io.File

data class WalletConfig(
  val walletUiUrl: String = "http://localhost:3000",
  val walletApiUrl: String = "http://localhost:3000/api",
  var issuers: Map<String, OIDCProvider> = mapOf()
) {
  companion object {
    val CONFIG_FILE = "${id.walt.WALTID_DATA_ROOT}/config/wallet-config.json"
    val ISSUERS_SECRETS = "${id.walt.WALTID_DATA_ROOT}/secrets/issuers.json"
    lateinit var config: WalletConfig
    init {
      val cf = File(CONFIG_FILE)
      if(cf.exists()) {
        config = Klaxon().parse<WalletConfig>(cf) ?: WalletConfig()
      } else {
        config = WalletConfig()
      }

      val issuerSecretsFile = File(ISSUERS_SECRETS)
      if(issuerSecretsFile.exists()) {
        val issuerSecrets = Klaxon().parse<SecretConfigMap>(issuerSecretsFile) ?: SecretConfigMap(mapOf())
        config.issuers = config.issuers.values.map { issuer ->
          Pair(issuer.id, issuer.withSecret(issuerSecrets.secrets[issuer.id]))
        }.toMap()
      }
    }
  }
}

