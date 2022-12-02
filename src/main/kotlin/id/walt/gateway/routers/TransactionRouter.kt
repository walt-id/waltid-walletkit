package id.walt.gateway.routers

import id.walt.gateway.controllers.TradeController
import io.javalin.apibuilder.ApiBuilder
import io.javalin.plugin.openapi.dsl.documented

object TransactionRouter: Router {
    override fun routes() {
        ApiBuilder.path("trades") {
            ApiBuilder.post("sell", documented(TradeController.sellDocs(), TradeController::sell))
            ApiBuilder.post("buy", documented(TradeController.buyDocs(), TradeController::buy))
            ApiBuilder.post("send", documented(TradeController.sendDocs(), TradeController::send))
            ApiBuilder.post("validate", documented(TradeController.validateDocs(), TradeController::validate))
        }
    }
}