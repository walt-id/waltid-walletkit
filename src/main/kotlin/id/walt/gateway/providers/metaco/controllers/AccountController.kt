package id.walt.gateway.providers.metaco.controllers

import id.walt.gateway.dto.AccountParameter
import id.walt.gateway.dto.BalanceData
import id.walt.gateway.dto.TransactionData
import id.walt.gateway.providers.metaco.AuthService
import id.walt.gateway.providers.metaco.MetacoClient
import id.walt.gateway.providers.metaco.restapi.account.AccountRepositoryImpl
import id.walt.gateway.usecases.AccountUseCase
import id.walt.gateway.usecases.TransactionUseCase
import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document

object AccountController {
    private val accountUseCase: AccountUseCase
    private val transactionUseCase: TransactionUseCase

    fun profile(ctx: Context) {
        val accountId = ctx.pathParam("accountId")
        ctx.json(accountUseCase.profile(AccountParameter("", accountId)))
    }

    fun balance(ctx: Context) {
        val accountId = ctx.pathParam("accountId")
        ctx.json(accountUseCase.balance(AccountParameter("", accountId)))
    }

    fun transactions(ctx: Context) {
        val accountId = ctx.pathParam("accountId")
        ctx.json(accountUseCase.transactions(AccountParameter("", accountId)))
    }

    fun profileDoc() = document().operation {
        it.summary("Returns the account profile data").operationId("profile").addTagsItem("Account Management")
    }.json<List<BalanceData>>("200") { it.description("The account profile data") }

    fun balanceDoc() = document().operation {
        it.summary("Returns the account balance").operationId("balance").addTagsItem("Account Management")
    }.json<List<BalanceData>>("200") { it.description("The account balance") }

    fun transactionDoc() = document().operation {
        it.summary("Returns the account transactions").operationId("transactions").addTagsItem("Account Management")
    }.json<List<TransactionData>>("200") { it.description("The account transactions") }

//    fun balanceDoc() =
//        document().operation {
//            it.summary("Returns the account balance").operationId("balance")
//                .addTagsItem("Account Management")
//        }.body<Account> {
//            it.description("Account to be onboarded.")
//        }.json<Boolean>("200") { it.description("The list of available tokens to be claimed.") }


}