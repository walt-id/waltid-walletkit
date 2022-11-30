package id.walt.gateway.controllers

import id.walt.gateway.dto.SellData
import id.walt.gateway.dto.SellParameter
import id.walt.gateway.providers.metaco.mockapi.TransactionUseCaseImpl
import id.walt.gateway.usecases.TransactionUseCase
import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document

object TransactionController {
    private val transactionUseCase: TransactionUseCase = TransactionUseCaseImpl()

    fun sell(ctx: Context) {
        val parameters = ctx.bodyAsClass<SellParameter>()
        ctx.json(transactionUseCase.sell(parameters))
    }

    fun sellDocs() = document().operation {
        it.summary("Returns the transaction trade details").operationId("sell").addTagsItem("Transaction Management")
    }.body<SellParameter> {
        it.description("Sell parameters")
    }.json<SellData>("200") { it.description("The transaction trade details") }
}