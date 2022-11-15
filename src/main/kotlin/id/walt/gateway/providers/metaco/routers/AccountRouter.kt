package id.walt.gateway.providers.metaco.routers

import id.walt.gateway.Router
import id.walt.gateway.providers.metaco.controllers.AccountController
import io.javalin.apibuilder.ApiBuilder
import io.javalin.plugin.openapi.dsl.documented

object AccountRouter: Router {
    override fun routes() {
        ApiBuilder.path("accounts") {
            ApiBuilder.get("{accountId}", documented(AccountController.profileDoc(), AccountController::profile))
            ApiBuilder.get("{accountId}/balance", documented(AccountController.balanceDoc(), AccountController::balance))
            ApiBuilder.get("{accountId}/transactions", documented(AccountController.transactionDoc(), AccountController::transactions))
        }
    }
}