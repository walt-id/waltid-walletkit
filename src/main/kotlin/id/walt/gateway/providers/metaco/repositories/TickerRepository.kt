package id.walt.gateway.providers.metaco.repositories

import id.walt.gateway.providers.metaco.restapi.ticker.model.Ticker

interface TickerRepository {
    fun findAll(criteria: Map<String, String>): List<Ticker>
    fun findById(tickerId: String): Ticker
}