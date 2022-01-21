package id.walt.webwallet.backend.auth

class UserInfo(
    val id: String
) {
    var email: String? = null
    var password: String? = null
    var token: String? = null
    var ethAccount: String? = null

    init {
        when {
            id.contains("@") -> email = id
            id.startsWith("0x") -> ethAccount = id
        }
    }
}
