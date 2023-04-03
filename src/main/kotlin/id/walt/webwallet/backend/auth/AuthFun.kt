package id.walt.webwallet.backend.auth

import org.bitcoinj.core.Base58

fun isPolkadotAccount(address: String): Boolean {
    try {
        // Convert the SS58 address to its binary representation
        val binaryAddress = Base58.decodeChecked(address).joinToString("") { byte ->
            String.format("%8s", Integer.toBinaryString(byte.toInt() and 0xFF)).replace(' ', '0')
        }

        // Get the first 8 bits (the prefix) and convert it to hex
        val prefix = binaryAddress.substring(0, 8).toInt(2).toString(16)

        // Check if the prefix is 0x00, which is the prefix for Polkadot accounts
        return prefix == "00"

    }catch(e: Exception) {}

    return false

}
