package id.walt.gateway.controllers

import id.walt.gateway.dto.TickerData
import id.walt.gateway.dto.TickerParameter
import id.walt.gateway.providers.coingecko.CoinRepositoryImpl
import id.walt.gateway.providers.coingecko.SimpleCoinUseCaseImpl
import id.walt.gateway.providers.coingecko.SimplePriceParser
import id.walt.gateway.providers.cryptoiconsapi.LogoUseCaseImpl
import id.walt.gateway.providers.metaco.restapi.TickerUseCaseImpl
import id.walt.gateway.providers.metaco.restapi.services.AuthService
import id.walt.gateway.providers.metaco.restapi.services.AuthSignatureService
import id.walt.gateway.providers.metaco.restapi.ticker.TickerRepositoryImpl
import id.walt.gateway.usecases.TickerUseCase
import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document

object TickerController {
    private val tickerUseCase: TickerUseCase = TickerUseCaseImpl(
        TickerRepositoryImpl(AuthService(AuthSignatureService())),
        SimpleCoinUseCaseImpl(CoinRepositoryImpl(), SimplePriceParser()),
        LogoUseCaseImpl()
    )

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

    fun getDoc() = document().operation {
        it.summary("Returns the ticker data").operationId("get").addTagsItem("Ticker Management")
    }.json<TickerData>("200") { it.description("The ticker data") }

    fun listDoc() = document().operation {
        it.summary("Returns the ticker list").operationId("list").addTagsItem("Ticker Management")
    }.json<List<TickerData>>("200") { it.description("The ticker list") }
}