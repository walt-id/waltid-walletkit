package id.walt.gateway.controllers

import id.walt.gateway.dto.AccountParameter
import id.walt.gateway.dto.BalanceData
import id.walt.gateway.dto.TransactionData
import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.providers.metaco.mockapi.AccountUseCaseImpl
import id.walt.gateway.providers.metaco.mockapi.TransactionUseCaseImpl
import id.walt.gateway.providers.metaco.restapi.AuthService
import id.walt.gateway.usecases.AccountUseCase
import id.walt.gateway.usecases.TransactionUseCase
import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document

object AccountController {
    private val authService = AuthService()
    private val accountUseCase: AccountUseCase =
        AccountUseCaseImpl()
//        AccountUseCaseImpl(
//            AccountRepositoryImpl(authService),
//            BalanceRepositoryImpl(authService),
//            TickerUseCaseImpl(
//                TickerRepositoryImpl(authService),
//                SimpleCoinUseCaseImpl(CoinRepositoryImpl(), SimplePriceParser()),
//                LogoUseCaseImpl()
//            )
//        )
    private val transactionUseCase: TransactionUseCase = TransactionUseCaseImpl()

    fun profile(ctx: Context) {
        val accountId = ctx.pathParam("accountId")
        accountUseCase.profile(AccountParameter("ProviderConfig.domainId", accountId))
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun balance(ctx: Context) {
        val accountId = ctx.pathParam("accountId")
        accountUseCase.balance(AccountParameter("ProviderConfig.domainId", accountId))
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun transactions(ctx: Context) {
        val accountId = ctx.pathParam("accountId")
        accountUseCase.transactions(AccountParameter("ProviderConfig.domainId", accountId))
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
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