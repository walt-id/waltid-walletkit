package id.walt.gateway.routers

import io.javalin.apibuilder.ApiBuilder


object MetacoMockRouter: Router {

    override fun routes() {
        ApiBuilder.path("mock") {
            AccountRouter.routes()
            TransactionRouter.routes()
            TickerRouter.routes()
        }
    }
}