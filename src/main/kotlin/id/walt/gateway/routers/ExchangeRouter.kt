package id.walt.gateway.routers

import id.walt.gateway.controllers.ExchangeController
import io.javalin.apibuilder.ApiBuilder
import io.javalin.plugin.openapi.dsl.documented

class ExchangeRouter(
    private val exchangeController: ExchangeController
) : Router {
    override fun routes() {
        ApiBuilder.path("exchange") {
            ApiBuilder.get("from/{from}/to/{to}/amount/{amount}/type/{type}", documented(exchangeController.getDoc(), exchangeController::get))
        }
    }
}