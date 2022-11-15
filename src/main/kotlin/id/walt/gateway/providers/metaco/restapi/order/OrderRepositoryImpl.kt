package id.walt.gateway.providers.metaco.restapi.order

import id.walt.gateway.providers.metaco.AuthService
import id.walt.gateway.providers.metaco.repositories.OrderRepository
import id.walt.gateway.providers.metaco.restapi.BaseRestRepository
import id.walt.gateway.providers.metaco.restapi.CommonHttp
import id.walt.gateway.providers.metaco.restapi.order.model.Order
import id.walt.gateway.providers.metaco.restapi.order.model.OrderList

class OrderRepositoryImpl(
    override val authService: AuthService
): OrderRepository, BaseRestRepository(authService) {
    private val listEndpoint = "/v1/domains/%s/transactions/orders%s"
    private val detailEndpoint = "/v1/domains/%s/transactions/orders/%s"

    override fun findAll(domainId: String, criteria: Map<String, String>) = CommonHttp.get<OrderList>(
        client,
        String.format(CommonHttp.buildUrl(baseUrl, listEndpoint), domainId, CommonHttp.buildQueryList(criteria))
    )

    override fun findById(domainId: String, orderId: String) = CommonHttp.get<Order>(
        client,
        String.format(
            CommonHttp.buildUrl(baseUrl, detailEndpoint),
            domainId,
            orderId
        )
    )
}