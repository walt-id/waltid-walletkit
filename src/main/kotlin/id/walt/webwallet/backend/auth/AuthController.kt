package id.walt.webwallet.backend.auth

import id.walt.model.DidMethod
import id.walt.services.context.ContextManager
import id.walt.services.did.DidService
import id.walt.webwallet.backend.context.WalletContextManager
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented

object AuthController {
    val routes
        get() = path("auth") {
            path("login") {
                post(documented(document().operation {
                    it.summary("Login")
                        .operationId("login")
                        .addTagsItem("Authentication")
                }
                    .body<UserInfo> { it.description("Login info") }
                    .json<UserInfo>("200"),
                    AuthController::login), UserRole.UNAUTHORIZED)
            }
            path("userInfo") {
                get(
                    documented(document().operation {
                        it.summary("Get current user info")
                            .operationId("userInfo")
                            .addTagsItem("Authentication")
                    }
                        .json<UserInfo>("200"),
                        AuthController::userInfo), UserRole.AUTHORIZED)
            }
        }

    fun login(ctx: Context) {
        val userInfo = ctx.bodyAsClass(UserInfo::class.java)
        // TODO: verify login credentials!!
        ContextManager.runWith(WalletContextManager.getUserContext(userInfo)) {
            if(DidService.listDids().isEmpty()) {
                DidService.create(DidMethod.key)
            }
        }
        ctx.json(UserInfo(userInfo.id).apply {
            token = JWTService.toJWT(userInfo)
        })
    }

    fun userInfo(ctx: Context) {
        ctx.json(JWTService.getUserInfo(ctx)!!)
    }
}
