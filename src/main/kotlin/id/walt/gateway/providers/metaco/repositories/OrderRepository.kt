package id.walt.gateway.providers.metaco.repositories

import id.walt.gateway.providers.metaco.restapi.order.model.Order
import id.walt.gateway.providers.metaco.restapi.order.model.OrderList

interface OrderRepository {
    fun findAll(domainId: String, criteria: Map<String, String>): OrderList
    fun findById(domainId: String, orderId: String): Order
}