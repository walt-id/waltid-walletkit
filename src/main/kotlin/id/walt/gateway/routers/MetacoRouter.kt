package id.walt.gateway.routers

import id.walt.gateway.controllers.*
import id.walt.gateway.providers.coingecko.SimpleCoinUseCaseImpl
import id.walt.gateway.providers.coingecko.SimplePriceParser
import id.walt.gateway.providers.cryptologos.LogoUseCaseImpl
import id.walt.gateway.providers.goldorg.GoldHistoricalPriceUseCaseImpl
import id.walt.gateway.providers.goldorg.HistoricalPriceRepository
import id.walt.gateway.providers.goldorg.HistoricalPriceRepositoryImpl
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
import id.walt.gateway.providers.rcb.StringFieldResponseParser
import id.walt.gateway.usecases.AccountUseCase
import id.walt.gateway.usecases.HistoricalPriceUseCase
import id.walt.gateway.usecases.MultiCoinUseCaseImpl
import id.walt.gateway.usecases.TradeUseCase
import io.javalin.apibuilder.ApiBuilder
import id.walt.gateway.providers.coingecko.CoinRepositoryImpl as CoingeckoImpl
import id.walt.gateway.providers.rcb.CoinRepositoryImpl as RcbImpl

object MetacoRouter : Router {
    private val authService = AuthService(AuthSignatureService())
    private val tickerRepository = TickerRepositoryImpl(authService)
    private val coinUseCase = MultiCoinUseCaseImpl(
        CoinUseCaseImpl(RcbImpl(), StringFieldResponseParser()),
        SimpleCoinUseCaseImpl(CoingeckoImpl(), SimplePriceParser()),
        RBITokensMockUseCaseImpl(),
    )
    private val requestUseCase = RequestUseCaseImpl(
        IntentRepositoryImpl(authService),
        IntentSignatureService(),
    )
    private val tickerUseCase = TickerUseCaseImpl(
        tickerRepository,
        LedgerRepositoryImpl(authService),
        coinUseCase,
        LogoUseCaseImpl(),
        requestUseCase,
    )
    private val transferRepository = TransferRepositoryImpl(authService)
    private val accountUseCase: AccountUseCase =
        AccountUseCaseImpl(
            DomainRepositoryImpl(authService),
            AccountRepositoryImpl(authService),
            TransactionRepositoryImpl(authService),
            OrderRepositoryImpl(authService),
            transferRepository,
            AddressRepositoryImpl(authService),
            BalanceUseCaseImpl(
                BalanceRepositoryImpl(authService),
                tickerUseCase,
            ),
            tickerUseCase,
            requestUseCase,
        )
    private val tradeUseCase: TradeUseCase =
        TradeUseCaseImpl(
            tickerUseCase,
            requestUseCase,
            transferRepository,
        )
    private val accountRouter = AccountRouter(AccountController(accountUseCase))
    private val transactionRouter = TransactionRouter(TradeController(tradeUseCase))
    private val tickerRouter = TickerRouter(TickerController(tickerUseCase))
    private val exchangeRouter = ExchangeRouter(ExchangeController(ExchangeUseCaseImpl(tickerRepository, coinUseCase)))
    private val historicalRouter = HistoricalDataRouter(HistoricalPriceController(GoldHistoricalPriceUseCaseImpl(HistoricalPriceRepositoryImpl())))

    override fun routes() {
        ApiBuilder.path("") {
            accountRouter.routes()
            transactionRouter.routes()
            tickerRouter.routes()
            exchangeRouter.routes()
            historicalRouter.routes()
        }
    }
}