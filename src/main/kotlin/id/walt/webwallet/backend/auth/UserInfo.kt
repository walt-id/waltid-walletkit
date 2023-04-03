package id.walt.webwallet.backend.auth

import kotlinx.serialization.Serializable
import org.bitcoinj.core.Base58

@Serializable
class UserInfo(
    val id: String
) {
    var email: String? = null
    var password: String? = null
    var token: String? = null
    var ethAccount: String? = null
    var did: String? = null
    var tezosAccount: String? = null
    var polkadotAccount: String? = null

    fun isPolkadotAccount(address: String): Boolean {
        // Convert the SS58 address to its binary representation
        val binaryAddress = Base58.decodeChecked(address).joinToString("") { byte ->
            String.format("%8s", Integer.toBinaryString(byte.toInt() and 0xFF)).replace(' ', '0')
        }

        // Get the first 8 bits (the prefix) and convert it to hex
        val prefix = binaryAddress.substring(0, 8).toInt(2).toString(16)

        // Check if the prefix is 0x00, which is the prefix for Polkadot accounts
        return prefix == "00"
    }
    
    init {
        when {
            id.contains("@") -> email = id
            id.lowercase().contains("0x") -> ethAccount = id
            id.lowercase().startsWith("did:") -> did = id
            id.lowercase().startsWith("tz") -> tezosAccount = id
            isPolkadotAccount(id) -> polkadotAccount = id
        }
    }
}
