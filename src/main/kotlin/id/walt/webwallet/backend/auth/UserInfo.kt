package id.walt.webwallet.backend.auth
import kotlinx.serialization.Serializable

@Serializable
class UserInfo(var id: String) {
    var email: String? = null
    var password: String? = null
    var token: String? = null
    var ethAccount: String? = null
    var did: String? = null
    var tezosAccount: String? = null
    var polkadotAccount: String? = null
    var polkadotEvmAccount: String? = null
    var flowAccount: String? = null
    var nearAccount: String? = null

    init {
        when {
            id.contains("@") -> email = id
            DidRegex.matches(id) -> {
                did = id
            }
            id.split("##")[0].lowercase() == "eth" -> {ethAccount = id.split("##")[1] }
            id.split("##")[0].lowercase() == "pol" -> {polkadotAccount = id.split("##")[1]}
            id.split("##")[0].lowercase() == "polevm" -> {polkadotEvmAccount = id.split("##")[1]}
            id.split("##")[0].lowercase() == "flow" -> {flowAccount = id.split("##")[1]}
            id.split("##")[0].lowercase() == "near" -> {nearAccount = id.split("##")[1]}
            id.lowercase().startsWith("tz") -> tezosAccount = id//TODO: tez##
        }
    }

    override fun equals(other: Any?): Boolean = (other as? UserInfo)?.takeIf {
        it.id == id
                && it.email.equals(email)
                && it.password.equals(password)
                && it.ethAccount.equals(ethAccount)
                && it.did.equals(did)
                && it.tezosAccount.equals(tezosAccount)
                && it.polkadotAccount.equals(polkadotAccount)
                && it.polkadotEvmAccount.equals(polkadotEvmAccount)
                && it.flowAccount.equals(flowAccount)
                && it.nearAccount.equals(nearAccount)
    }?.let { true } ?: super.equals(other)
}

val DidRegex = Regex("did:.+:.+")