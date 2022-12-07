package id.walt.gateway.controllers

import id.walt.gateway.dto.AccountBalance
import id.walt.gateway.dto.TickerParameter
import id.walt.gateway.providers.metaco.mockapi.TickerUseCaseImpl
import id.walt.gateway.usecases.TickerUseCase
import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document

object TickerController {
    private val tickerUseCase: TickerUseCase = TickerUseCaseImpl()

    fun get(ctx: Context) {
        val tickerId = ctx.pathParam("tickerId")
        tickerUseCase.get(TickerParameter(tickerId))
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun getDoc() = document().operation {
        it.summary("Returns the ticker data").operationId("get").addTagsItem("Ticker Management")
    }.json<AccountBalance>("200") { it.description("The ticker data") }
}