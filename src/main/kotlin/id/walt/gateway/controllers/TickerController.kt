package id.walt.gateway.controllers

import id.walt.gateway.dto.requests.RequestResult
import id.walt.gateway.dto.tickers.FeeData
import id.walt.gateway.dto.tickers.TickerData
import id.walt.gateway.dto.tickers.TickerParameter
import id.walt.gateway.usecases.TickerUseCase
import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document

class TickerController(
    private val tickerUseCase: TickerUseCase,
) {
//    private val tickerUseCase: TickerUseCase = TickerUseCaseImpl(
//        TickerRepositoryImpl(AuthService(AuthSignatureService())),
//        SimpleCoinUseCaseImpl(CoinRepositoryImpl(), SimplePriceParser()),
//        LogoUseCaseImpl()
//    )

    fun get(ctx: Context) {
        val tickerId = ctx.pathParam("tickerId")
        tickerUseCase.get(TickerParameter(tickerId))
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun list(ctx: Context) {
        tickerUseCase.list("eur")
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun fee(ctx: Context) {
        val tickerId = ctx.pathParam("tickerId")
        tickerUseCase.fee(tickerId)
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun validate(ctx: Context) {
        val tickerId = ctx.pathParam("tickerId")
        tickerUseCase.validate(tickerId)
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun getDoc() = document().operation {
        it.summary("Returns the ticker data").operationId("get").addTagsItem("Ticker Management")
    }.json<TickerData>("200") { it.description("The ticker data") }

    fun listDoc() = document().operation {
        it.summary("Returns the ticker list").operationId("list").addTagsItem("Ticker Management")
    }.json<List<TickerData>>("200") { it.description("The ticker list") }

    fun feeDoc() = document().operation {
        it.summary("Returns the ticker fee data").operationId("fee").addTagsItem("Ticker Management")
    }.json<FeeData>("200") { it.description("The ticker fee data") }

    fun validateDoc() = document().operation {
        it.summary("Returns the ticker validation request result").operationId("fee").addTagsItem("Ticker Management")
    }.json<RequestResult>("200") { it.description("The ticker validation request result") }
}