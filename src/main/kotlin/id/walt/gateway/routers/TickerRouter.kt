package id.walt.gateway.routers

import id.walt.gateway.controllers.TickerController
import io.javalin.apibuilder.ApiBuilder
import io.javalin.plugin.openapi.dsl.documented

object TickerRouter : Router {
    override fun routes() {
        ApiBuilder.path("tickers") {
            ApiBuilder.get("{tickerId}", documented(TickerController.getDoc(), TickerController::get))
        }
    }
}