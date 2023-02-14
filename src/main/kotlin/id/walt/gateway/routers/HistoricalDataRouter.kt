package id.walt.gateway.routers

import id.walt.gateway.controllers.HistoricalPriceController
import io.javalin.apibuilder.ApiBuilder
import io.javalin.plugin.openapi.dsl.documented

class HistoricalDataRouter(
    private val historicalPriceController: HistoricalPriceController,
): Router {
    override fun routes() {
        ApiBuilder.path("historical") {
            ApiBuilder.get("asset/{asset}/timeframe/{timeframe}", documented(historicalPriceController.getDoc(), historicalPriceController::get))
        }
    }
}