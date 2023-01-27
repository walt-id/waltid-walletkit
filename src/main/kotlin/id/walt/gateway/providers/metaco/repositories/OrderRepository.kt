package id.walt.gateway.providers.metaco.repositories

import id.walt.gateway.providers.metaco.restapi.order.model.Order

interface OrderRepository {
    fun findAll(domainId: String, criteria: Map<String, String>): List<Order>
    fun findById(domainId: String, orderId: String): Order
}