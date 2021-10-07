package id.walt.webwallet.backend.auth

import io.javalin.apibuilder.ApiBuilder
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.Context

object AuthController {
  val routes
    get() =  path("auth") {
    path("login") {
      post(AuthController::login, UserRole.UNAUTHORIZED)
    }
    path("userInfo") {
      get(AuthController::userInfo, UserRole.AUTHORIZED)
    }
  }

  fun login(ctx: Context) {
    val userInfo =  ctx.bodyAsClass(UserInfo::class.java)
    // TODO: verify login credentials!!
    ctx.json(UserInfo(userInfo.email).apply { token = JWTService.toJWT(userInfo) })
  }

  fun userInfo(ctx: Context) {
    ctx.json(JWTService.getUserInfo(ctx))
  }
}