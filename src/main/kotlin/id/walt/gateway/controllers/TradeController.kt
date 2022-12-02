package id.walt.gateway.controllers

import id.walt.gateway.dto.trades.*
import id.walt.gateway.providers.metaco.mockapi.TradeUseCaseImpl
import id.walt.gateway.usecases.TradeUseCase
import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document

object TradeController {
    private val tradeUseCase: TradeUseCase = TradeUseCaseImpl()

    fun sell(ctx: Context) {
        val parameters = ctx.bodyAsClass<SellParameter>()
        tradeUseCase.sell(parameters)
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun buy(ctx: Context) {
        val parameters = ctx.bodyAsClass<BuyParameter>()
        tradeUseCase.buy(parameters)
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun send(ctx: Context) {
        val parameters = ctx.bodyAsClass<SendParameter>()
        tradeUseCase.send(parameters)
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun validate(ctx: Context) {
        val parameters = ctx.bodyAsClass<TradePreviewParameter>()
        tradeUseCase.validate(parameters)
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun sellDocs() = document().operation {
        it.summary("Returns the sell trade details").operationId("sell").addTagsItem("Trade Management")
    }.body<SellParameter> {
        it.description("Sell parameters")
    }.json<SellData>("200") { it.description("The sell trade details") }

    fun buyDocs() = document().operation {
        it.summary("Returns the buy trade details").operationId("buy").addTagsItem("Trade Management")
    }.body<BuyParameter> {
        it.description("Buy parameters")
    }.json<SellData>("200") { it.description("The buy trade details") }

    fun sendDocs() = document().operation {
        it.summary("Returns the send trade details").operationId("send").addTagsItem("Trade Management")
    }.body<SendParameter> {
        it.description("Send parameters")
    }.json<SendData>("200") { it.description("The send trade details") }

    fun validateDocs() = document().operation {
        it.summary("Returns the trade validation details").operationId("validate").addTagsItem("Trade Management")
    }.body<TradePreviewParameter> {
        it.description("Trade preview parameters")
    }.json<TradePreviewValidation>("200") { it.description("The trade validation details") }
}