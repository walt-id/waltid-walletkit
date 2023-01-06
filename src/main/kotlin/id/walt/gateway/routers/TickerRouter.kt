package id.walt.gateway.routers

import id.walt.gateway.controllers.TickerController
import io.javalin.apibuilder.ApiBuilder
import io.javalin.plugin.openapi.dsl.documented

class TickerRouter(
    private val tickerController: TickerController,
) : Router {
    override fun routes() {
        ApiBuilder.path("tickers") {
            ApiBuilder.get("", documented(tickerController.listDoc(), tickerController::list))
            ApiBuilder.get("{tickerId}", documented(tickerController.getDoc(), tickerController::get))
        }
    }
}