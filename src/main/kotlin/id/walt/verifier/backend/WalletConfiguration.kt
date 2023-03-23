package id.walt.verifier.backend

import id.walt.webwallet.backend.config.ExternalHostnameUrl

data class WalletConfiguration(
    val id: String,
    @ExternalHostnameUrl val url: String,
    val presentPath: String,
    val receivePath: String,
    val description: String
) {

    companion object {
        fun getDefaultWalletConfigurations(defaultWalletUrl: String = "http://localhost:3000"): Map<String, WalletConfiguration> {
            return mapOf(
                Pair(
                    "walt.id",
                    WalletConfiguration(
                        "walt.id",
                        defaultWalletUrl,
                        "api/siop/initiatePresentation",
                        "api/siop/initiateIssuance",
                        "walt.id web wallet"
                    )
                )
            )
        }
    }
}
