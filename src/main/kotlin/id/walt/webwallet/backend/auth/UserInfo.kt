package id.walt.webwallet.backend.auth

import kotlinx.serialization.Serializable

@Serializable
class UserInfo(var id: String ) {
    var email: String? = null
    var password: String? = null
    var token: String? = null
    var ethAccount: String? = null
    var did: String? = null
    var tezosAccount: String? = null
    var polkadotAccount: String? = null
    var polkadotEvmAccount: String? = null
    var flowAccount: String? = null

    init {
        when {
            id.contains("@") -> email = id
            id.startsWith("did") -> {
                did = id
            }
            id.split("##")[0].lowercase().startsWith("eth") -> {ethAccount = id.split("##")[1] }
            id.split("##")[0].lowercase().startsWith("pol") -> {polkadotAccount = id.split("##")[1]}
            id.split("##")[0].lowercase().startsWith("polevm") -> {polkadotEvmAccount = id.split("##")[1]}
            id.split("##")[0].lowercase().startsWith("flow") -> {flowAccount = id.split("##")[1]}
            id.lowercase().startsWith("tz") -> tezosAccount = id

        }

    }
}
