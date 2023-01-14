package id.walt.gateway.controllers

import id.walt.gateway.dto.balances.AccountBalance
import id.walt.gateway.dto.balances.BalanceData
import id.walt.gateway.dto.balances.BalanceParameter
import id.walt.gateway.dto.profiles.ProfileData
import id.walt.gateway.dto.profiles.ProfileParameter
import id.walt.gateway.dto.transactions.TransactionData
import id.walt.gateway.dto.transactions.TransactionListParameter
import id.walt.gateway.dto.transactions.TransactionParameter
import id.walt.gateway.dto.transactions.TransactionTransferData
import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.usecases.AccountUseCase
import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document

class AccountController(
    private val accountUseCase: AccountUseCase,
) {
//    private val authService = AuthService(AuthSignatureService())
//    private val tickerUseCase = TickerUseCaseImpl(
//        TickerRepositoryImpl(authService),
//        SimpleCoinUseCaseImpl(CoinRepositoryImpl(), SimplePriceParser()),
//        LogoUseCaseImpl()
//    )
//    private val accountUseCase: AccountUseCase =
////        AccountUseCaseImpl()
//        AccountUseCaseImpl(
//            AccountRepositoryImpl(authService),
//            TransactionRepositoryImpl(authService),
//            OrderRepositoryImpl(authService),
//            TransferRepositoryImpl(authService),
//            AddressRepositoryImpl(authService),
//            BalanceUseCaseImpl(
//                BalanceRepositoryImpl(authService),
//                tickerUseCase,
//            ),
//            tickerUseCase
//        )

    fun profile(ctx: Context) {
        val profile = ctx.bodyAsClass<ProfileParameter>()
        accountUseCase.profile(ProviderConfig.domainId, profile)
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun balance(ctx: Context) {
        val profileId = ctx.pathParam("profileId")
        accountUseCase.balance(ProviderConfig.domainId, ProfileParameter(profileId))
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun tickerBalance(ctx: Context) {
        val accountId = ctx.pathParam("accountId")
        val tickerId = ctx.pathParam("tickerId")
        accountUseCase.balance(BalanceParameter(ProviderConfig.domainId, accountId, tickerId))
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun transactions(ctx: Context) {
        val accountId = ctx.pathParam("accountId")
        val tickerId = ctx.queryParam("tickerId")
        accountUseCase.transactions(TransactionListParameter(ProviderConfig.domainId, accountId, tickerId))
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
                ProviderConfig.domainId, transactionId, mapOf("accountId" to accountId)
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
    }.json<ProfileData>("200") { it.description("The account profile data") }

    fun balanceDoc() = document().operation {
        it.summary("Returns the account balance").operationId("balance").addTagsItem("Account Management")
    }.json<AccountBalance>("200") { it.description("The account balance") }

    fun tickerBalanceDoc() = document().operation {
        it.summary("Returns the account balance for ticker").operationId("tickerBalance")
            .addTagsItem("Account Management")
    }.json<BalanceData>("200") { it.description("The account balance for ticker") }

    fun transactionsDoc() = document().operation {
        it.summary("Returns the account transactions").operationId("transactions").addTagsItem("Account Management")
    }.queryParam<String>("tickerId").json<List<TransactionData>>("200") { it.description("The account transactions") }

    fun transactionDoc() = document().operation {
        it.summary("Returns the transaction transfers").operationId("transaction").addTagsItem("Account Management")
    }.json<TransactionTransferData>("200") { it.description("The transaction transfers") }

}