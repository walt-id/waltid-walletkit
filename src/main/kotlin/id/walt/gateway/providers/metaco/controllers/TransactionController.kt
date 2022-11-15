package id.walt.gateway.providers.metaco.controllers

import id.walt.gateway.providers.metaco.MetacoClient
import id.walt.gateway.usecases.TransactionUseCase
import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document

object TransactionController {
    private val transactionUseCase = TransactionUseCase()

    fun detail(ctx: Context) {
        val transactionId = ctx.pathParam("transactionId")
        ctx.json(transactionUseCase.detail(transactionId, MetacoClient.domainId))
    }

    fun detailDocs() = document().operation {
        it.summary("Returns the transaction trade details").operationId("detail").addTagsItem("Transaction Management")
    }.json<Transfer>("200") { it.description("The transaction trade details") }
}