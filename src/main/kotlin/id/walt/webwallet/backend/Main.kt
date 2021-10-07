package id.walt.webwallet.backend

import id.walt.webwallet.backend.auth.AuthController
import id.walt.webwallet.backend.auth.JWTService
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import javalinjwt.JWTAccessManager

fun main(args: Array<String>) {
    val app = Javalin.create{ config ->
        config.accessManager(JWTService)
    }.start(8080)
    app.before(JWTService.jwtHandler)

    app.routes {
        path("api") {
            AuthController.routes
        }
    }
}
