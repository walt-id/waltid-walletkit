package id.walt.gateway.providers.metaco.restapi.order

import id.walt.gateway.CommonHttp
import id.walt.gateway.providers.metaco.repositories.OrderRepository
import id.walt.gateway.providers.metaco.restapi.BaseRestRepository
import id.walt.gateway.providers.metaco.restapi.order.model.Order
import id.walt.gateway.providers.metaco.restapi.order.model.OrderList
import id.walt.gateway.providers.metaco.restapi.services.AuthService

class OrderRepositoryImpl(
    override val authService: AuthService
) : OrderRepository, BaseRestRepository(authService) {
    private val listEndpoint = "/v1/domains/%s/transactions/orders%s"
    private val detailEndpoint = "/v1/domains/%s/transactions/orders/%s"

    override fun findAll(domainId: String, criteria: Map<String, String>): List<Order> =
        findAllLoopPages<OrderList, Order>(String.format(CommonHttp.buildUrl(baseUrl, listEndpoint), domainId), criteria)

    override fun findById(domainId: String, orderId: String) = CommonHttp.get<Order>(
        client,
        String.format(
            CommonHttp.buildUrl(baseUrl, detailEndpoint),
            domainId,
            orderId
        )
    )
}