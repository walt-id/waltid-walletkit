package id.walt.gateway.controllers

import id.walt.gateway.dto.*
import id.walt.gateway.dto.trades.TradeListParameter
import id.walt.gateway.providers.metaco.mockapi.AccountUseCaseImpl
import id.walt.gateway.providers.metaco.restapi.AuthService
import id.walt.gateway.providers.metaco.restapi.signservice.AuthSignatureService
import id.walt.gateway.usecases.AccountUseCase
import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document

object AccountController {
    private val authService = AuthService(AuthSignatureService())
    private val accountUseCase: AccountUseCase =
        AccountUseCaseImpl()
//        AccountUseCaseImpl(
//            AccountRepositoryImpl(authService),
//            BalanceUseCaseImpl(
//                BalanceRepositoryImpl(authService),
//                TickerUseCaseImpl(
//                    TickerRepositoryImpl(authService),
//                    SimpleCoinUseCaseImpl(CoinRepositoryImpl(), SimplePriceParser()),
//                    LogoUseCaseImpl()
//                )
//            )
//        )

    fun profile(ctx: Context) {
        val account = ctx.bodyAsClass<ProfileParameter>()
        accountUseCase.profile(AccountParameter("ProviderConfig.domainId", account.id))
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

    fun tickerBalance(ctx: Context) {
        val accountId = ctx.pathParam("accountId")
        val tickerId = ctx.pathParam("tickerId")
        accountUseCase.balance(BalanceParameter("ProviderConfig.domainId", accountId, tickerId))
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun transactions(ctx: Context) {
        val accountId = ctx.pathParam("accountId")
        val tickerId = ctx.queryParam("tickerId")
        accountUseCase.transactions(TradeListParameter("ProviderConfig.domainId", accountId, tickerId))
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun transaction(ctx: Context) {
        val accountId = ctx.pathParam("accountId")
        val transactionId = ctx.pathParam("transactionId")
        accountUseCase.transaction(
            TransactionParameter(
                "ProviderConfig.domainId", transactionId, mapOf("accountId" to accountId)
            )
        ).onSuccess {
            ctx.json(it)
        }.onFailure {
            ctx.json(it)
        }
    }

    fun profileDoc() = document().operation {
        it.summary("Returns the account profile data").operationId("profile").addTagsItem("Account Management")
    }.body<ProfileParameter> {
        it.description("Profile parameter.")
    }.json<List<ProfileData>>("200") { it.description("The account profile data") }

    fun balanceDoc() = document().operation {
        it.summary("Returns the account balance").operationId("balance").addTagsItem("Account Management")
    }.json<AccountBalance>("200") { it.description("The account balance") }

    fun tickerBalanceDoc() = document().operation {
        it.summary("Returns the account balance for ticker").operationId("tickerBalance")
            .addTagsItem("Account Management")
    }.json<BalanceData>("200") { it.description("The account balance for ticker") }

    fun transactionsDoc() = document().operation {
        it.summary("Returns the account transactions").operationId("transactions").addTagsItem("Account Management")
    }.json<List<TransactionData>>("200") { it.description("The account transactions") }

    fun transactionDoc() = document().operation {
        it.summary("Returns the transaction transfers").operationId("transaction").addTagsItem("Account Management")
    }.json<TransactionTransferData>("200") { it.description("The transaction transfers") }

}