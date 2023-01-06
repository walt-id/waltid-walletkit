package id.walt.gateway.routers

import id.walt.gateway.controllers.AccountController
import id.walt.gateway.controllers.TickerController
import id.walt.gateway.controllers.TradeController
import id.walt.gateway.providers.metaco.mockapi.AccountUseCaseImpl
import id.walt.gateway.providers.metaco.mockapi.TickerUseCaseImpl
import id.walt.gateway.providers.metaco.mockapi.TradeUseCaseImpl
import io.javalin.apibuilder.ApiBuilder


object MetacoMockRouter : Router {

    private val tickerUseCase = TickerUseCaseImpl()
    private val accountRouter = AccountRouter(AccountController(AccountUseCaseImpl(tickerUseCase)))
    private val transactionRouter = TransactionRouter(TradeController(TradeUseCaseImpl()))
    private val tickerRouter = TickerRouter(TickerController(tickerUseCase))

    override fun routes() {
        ApiBuilder.path("mock") {
            accountRouter.routes()
            transactionRouter.routes()
            tickerRouter.routes()
        }
    }
}