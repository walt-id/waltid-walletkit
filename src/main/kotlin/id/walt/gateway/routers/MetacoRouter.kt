package id.walt.gateway.routers

import io.javalin.apibuilder.ApiBuilder

object MetacoRouter : Router {
    override fun routes() {
        ApiBuilder.path("") {
            AccountRouter.routes()
            TransactionRouter.routes()
            TickerRouter.routes()
        }
    }
}