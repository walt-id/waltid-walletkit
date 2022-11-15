package id.walt.gateway.providers.metaco.routers

import id.walt.gateway.Router
import io.javalin.apibuilder.ApiBuilder

object MetacoRouter : Router {
    override fun routes() {
        ApiBuilder.path("") {
            AccountRouter.routes()
            TransactionRouter.routes()
        }
    }
}