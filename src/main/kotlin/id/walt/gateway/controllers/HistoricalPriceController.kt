package id.walt.gateway.controllers

import id.walt.gateway.dto.HistoricalPriceData
import id.walt.gateway.dto.HistoricalPriceParameter
import id.walt.gateway.usecases.HistoricalPriceUseCase
import io.javalin.http.Context
import io.javalin.http.HttpCode
import io.javalin.plugin.openapi.dsl.document

class HistoricalPriceController(
    private val historicalPriceUseCase: HistoricalPriceUseCase,
) {
    fun get(ctx: Context) {
        val parameter = HistoricalPriceParameter(
            timeframe = ctx.pathParam("timeframe"),
            asset = ctx.pathParam("asset"),
        )
        val result = historicalPriceUseCase.get(parameter).getOrDefault(emptyList())
        ctx.status(HttpCode.OK)
        ctx.json(result)
    }

    fun getDoc() = document().operation {
        it.summary("Returns the list of historical price data").operationId("get").addTagsItem("Historical Data Management")
    }.json<List<HistoricalPriceData>>("200") { it.description("The historical price data") }
}