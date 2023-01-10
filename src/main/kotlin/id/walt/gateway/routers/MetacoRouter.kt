package id.walt.gateway.routers

import id.walt.gateway.controllers.AccountController
import id.walt.gateway.controllers.TickerController
import id.walt.gateway.controllers.TradeController
import id.walt.gateway.providers.MultiCoinUseCaseImpl
import id.walt.gateway.providers.coingecko.CoinRepositoryImpl
import id.walt.gateway.providers.coingecko.SimpleCoinUseCaseImpl
import id.walt.gateway.providers.coingecko.SimplePriceParser
import id.walt.gateway.providers.cryptologos.LogoUseCaseImpl
import id.walt.gateway.providers.metaco.restapi.AccountUseCaseImpl
import id.walt.gateway.providers.metaco.restapi.BalanceUseCaseImpl
import id.walt.gateway.providers.metaco.restapi.TickerUseCaseImpl
import id.walt.gateway.providers.metaco.restapi.TradeUseCaseImpl
import id.walt.gateway.providers.metaco.restapi.account.AccountRepositoryImpl
import id.walt.gateway.providers.metaco.restapi.address.AddressRepositoryImpl
import id.walt.gateway.providers.metaco.restapi.balance.BalanceRepositoryImpl
import id.walt.gateway.providers.metaco.restapi.intent.IntentRepositoryImpl
import id.walt.gateway.providers.metaco.restapi.order.OrderRepositoryImpl
import id.walt.gateway.providers.metaco.restapi.services.AuthService
import id.walt.gateway.providers.metaco.restapi.services.AuthSignatureService
import id.walt.gateway.providers.metaco.restapi.services.IntentSignatureService
import id.walt.gateway.providers.metaco.restapi.ticker.TickerRepositoryImpl
import id.walt.gateway.providers.metaco.restapi.transaction.TransactionRepositoryImpl
import id.walt.gateway.providers.metaco.restapi.transfer.TransferRepositoryImpl
import id.walt.gateway.providers.mockcoin.RBITokensMockUseCaseImpl
import id.walt.gateway.usecases.AccountUseCase
import id.walt.gateway.usecases.TradeUseCase
import io.javalin.apibuilder.ApiBuilder

object MetacoRouter : Router {
    private val authService = AuthService(AuthSignatureService())
    private val tickerRepository = TickerRepositoryImpl(authService)
    private val tickerUseCase = TickerUseCaseImpl(
        tickerRepository,
        MultiCoinUseCaseImpl(
            RBITokensMockUseCaseImpl(),
            SimpleCoinUseCaseImpl(CoinRepositoryImpl(), SimplePriceParser()),
        ),
//        SimpleCoinUseCaseImpl(CoinRepositoryImpl(), SimplePriceParser()),
        LogoUseCaseImpl()
    )
    private val accountUseCase: AccountUseCase =
        AccountUseCaseImpl(
            AccountRepositoryImpl(authService),
            TransactionRepositoryImpl(authService),
            OrderRepositoryImpl(authService),
            TransferRepositoryImpl(authService),
            AddressRepositoryImpl(authService),
            BalanceUseCaseImpl(
                BalanceRepositoryImpl(authService),
                tickerUseCase,
            ),
            tickerUseCase
        )
    private val tradeUseCase: TradeUseCase =
        TradeUseCaseImpl(
            IntentRepositoryImpl(authService),
            tickerRepository,
            IntentSignatureService()
        )
    private val accountRouter = AccountRouter(AccountController(accountUseCase))
    private val transactionRouter = TransactionRouter(TradeController(tradeUseCase))
    private val tickerRouter = TickerRouter(TickerController(tickerUseCase))

    override fun routes() {
        ApiBuilder.path("") {
            accountRouter.routes()
            transactionRouter.routes()
            tickerRouter.routes()
        }
    }
}