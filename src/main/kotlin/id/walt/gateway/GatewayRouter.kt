package id.walt.gateway

import id.walt.gateway.providers.metaco.routers.MetacoRouter

object GatewayRouter: Router {
    override fun routes() {
        MetacoRouter.routes()
    }
}