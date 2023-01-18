package id.walt.gateway.controllers

import id.walt.gateway.dto.exchanges.ExchangeData
import id.walt.gateway.dto.exchanges.ExchangeParameter
import id.walt.gateway.usecases.ExchangeUseCase
import io.javalin.http.Context
import io.javalin.http.HttpCode
import io.javalin.plugin.openapi.dsl.document

class ExchangeController(
    private val exchangeUseCase: ExchangeUseCase,
) {
    fun get(ctx: Context) {
        val parameter = ExchangeParameter(
            from = ctx.pathParam("from"),
            to = ctx.pathParam("to"),
            amount = ctx.pathParam("amount")
        )
        val result = exchangeUseCase.exchange(parameter).getOrDefault(ExchangeData("0", "0"))
        ctx.status(HttpCode.OK)
        ctx.json(result)
    }

    fun getDoc() = document().operation {
        it.summary("Returns the exchange data").operationId("get").addTagsItem("Exchange Management")
    }.json<ExchangeData>("200") { it.description("The exchange data") }
}