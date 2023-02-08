package id.walt.gateway.routers

import id.walt.gateway.controllers.TradeController
import io.javalin.apibuilder.ApiBuilder
import io.javalin.plugin.openapi.dsl.documented

class TransactionRouter(
    private val tradeController: TradeController,
) : Router {
    override fun routes() {
        ApiBuilder.path("trades") {
            ApiBuilder.post("sell", documented(tradeController.sellDocs(), tradeController::sell))
            ApiBuilder.post("buy", documented(tradeController.buyDocs(), tradeController::buy))
            ApiBuilder.post("send", documented(tradeController.sendDocs(), tradeController::send))
            ApiBuilder.post("validate", documented(tradeController.validateDocs(), tradeController::validate))
            ApiBuilder.post("airdrop", documented(tradeController.airdropDocs(), tradeController::airdrop))
        }
    }
}