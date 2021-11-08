package id.walt.webwallet.backend.auth

import io.javalin.core.security.RouteRole

enum class UserRole : RouteRole {
    UNAUTHORIZED,
    AUTHORIZED
}
