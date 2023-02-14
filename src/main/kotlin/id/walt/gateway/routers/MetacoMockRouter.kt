package id.walt.gateway.routers

import id.walt.gateway.controllers.*
import id.walt.gateway.providers.metaco.mockapi.*
import io.javalin.apibuilder.ApiBuilder


object MetacoMockRouter : Router {

    private val tickerUseCase = TickerUseCaseImpl()
    private val accountRouter = AccountRouter(AccountController(AccountUseCaseImpl(tickerUseCase)))
    private val transactionRouter = TransactionRouter(TradeController(TradeUseCaseImpl()))
    private val tickerRouter = TickerRouter(TickerController(tickerUseCase))
    private val exchangeRouter = ExchangeRouter(ExchangeController(ExchangeUseCaseImpl()))
    private val historicalRouter = HistoricalDataRouter(HistoricalPriceController(GoldHistoricalPriceUseCaseImpl()))

    override fun routes() {
        ApiBuilder.path("mock") {
            accountRouter.routes()
            transactionRouter.routes()
            tickerRouter.routes()
            exchangeRouter.routes()
            historicalRouter.routes()
        }
    }
}