package id.walt.gateway.routers

import id.walt.gateway.controllers.TransactionController
import io.javalin.apibuilder.ApiBuilder
import io.javalin.plugin.openapi.dsl.documented

object TransactionRouter: Router {
    override fun routes() {
        ApiBuilder.path("transactions") {
            ApiBuilder.post("sell", documented(TransactionController.sellDocs(), TransactionController::sell))
        }
    }
}