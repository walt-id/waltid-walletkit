package id.walt.webwallet.backend.auth

class UserInfo(
    val email: String
) {
    var password: String? = null
    var token: String? = null
}
