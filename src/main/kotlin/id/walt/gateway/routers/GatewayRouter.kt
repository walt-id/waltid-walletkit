package id.walt.gateway.routers

object GatewayRouter: Router {
    override fun routes() {
        MetacoRouter.routes()
    }
}