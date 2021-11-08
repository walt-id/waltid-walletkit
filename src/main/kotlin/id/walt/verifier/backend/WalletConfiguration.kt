package id.walt.verifier.backend

data class WalletConfiguration(
    val id: String,
    val url: String,
    val presentPath: String,
    val description: String
) {

    companion object {
        fun getDefaultWalletConfigurations(): Map<String, WalletConfiguration> {
            return mapOf(
                Pair("walt.id", WalletConfiguration("walt.id", "http://localhost:3000", "verify", "walt.id web wallet"))
            )
        }
    }
}
