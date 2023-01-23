package id.walt.gateway.routers

import id.walt.gateway.controllers.AccountController
import id.walt.gateway.controllers.ExchangeController
import id.walt.gateway.controllers.TickerController
import id.walt.gateway.controllers.TradeController
import id.walt.gateway.providers.coingecko.SimpleCoinUseCaseImpl
import id.walt.gateway.providers.coingecko.SimplePriceParser
import id.walt.gateway.providers.cryptologos.LogoUseCaseImpl
import id.walt.gateway.providers.metaco.mockapi.RBITokensMockUseCaseImpl
import id.walt.gateway.providers.metaco.restapi.*
import id.walt.gateway.providers.metaco.restapi.account.AccountRepositoryImpl
import id.walt.gateway.providers.metaco.restapi.address.AddressRepositoryImpl
import id.walt.gateway.providers.metaco.restapi.balance.BalanceRepositoryImpl
import id.walt.gateway.providers.metaco.restapi.domain.DomainRepositoryImpl
import id.walt.gateway.providers.metaco.restapi.intent.IntentRepositoryImpl
import id.walt.gateway.providers.metaco.restapi.ledger.LedgerRepositoryImpl
import id.walt.gateway.providers.metaco.restapi.order.OrderRepositoryImpl
import id.walt.gateway.providers.metaco.restapi.services.AuthService
import id.walt.gateway.providers.metaco.restapi.services.AuthSignatureService
import id.walt.gateway.providers.metaco.restapi.services.IntentSignatureService
import id.walt.gateway.providers.metaco.restapi.ticker.TickerRepositoryImpl
import id.walt.gateway.providers.metaco.restapi.transaction.TransactionRepositoryImpl
import id.walt.gateway.providers.metaco.restapi.transfer.TransferRepositoryImpl
import id.walt.gateway.providers.rcb.CoinUseCaseImpl
import id.walt.gateway.providers.rcb.DoubleFieldResponseParser
import id.walt.gateway.usecases.AccountUseCase
import id.walt.gateway.usecases.MultiCoinUseCaseImpl
import id.walt.gateway.usecases.TradeUseCase
import io.javalin.apibuilder.ApiBuilder
import id.walt.gateway.providers.coingecko.CoinRepositoryImpl as CoingeckoImpl
import id.walt.gateway.providers.rcb.CoinRepositoryImpl as RcbImpl

object MetacoRouter : Router {
    private val authService = AuthService(AuthSignatureService())
    private val tickerRepository = TickerRepositoryImpl(authService)
    private val coinUseCase = MultiCoinUseCaseImpl(
        CoinUseCaseImpl(RcbImpl(), DoubleFieldResponseParser()),
        SimpleCoinUseCaseImpl(CoingeckoImpl(), SimplePriceParser()),
        RBITokensMockUseCaseImpl(),
    )
    private val tickerUseCase = TickerUseCaseImpl(
        tickerRepository,
        LedgerRepositoryImpl(authService),
        coinUseCase,
        LogoUseCaseImpl()
    )
    private val accountUseCase: AccountUseCase =
        AccountUseCaseImpl(
            DomainRepositoryImpl(authService),
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
            tickerRepository,
            RequestUseCaseImpl(
                IntentRepositoryImpl(authService),
                IntentSignatureService(),
            ),
        )
    private val accountRouter = AccountRouter(AccountController(accountUseCase))
    private val transactionRouter = TransactionRouter(TradeController(tradeUseCase))
    private val tickerRouter = TickerRouter(TickerController(tickerUseCase))
    private val exchangeRouter = ExchangeRouter(ExchangeController(ExchangeUseCaseImpl(tickerRepository, coinUseCase)))

    override fun routes() {
        ApiBuilder.path("") {
            accountRouter.routes()
            transactionRouter.routes()
            tickerRouter.routes()
            exchangeRouter.routes()
        }
    }
}