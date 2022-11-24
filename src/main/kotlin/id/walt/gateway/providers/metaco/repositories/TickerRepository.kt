package id.walt.gateway.providers.metaco.repositories

import id.walt.gateway.providers.metaco.restapi.ticker.model.Ticker
import id.walt.gateway.providers.metaco.restapi.ticker.model.TickerList

interface TickerRepository {
    fun findAll(criteria: Map<String, String>): TickerList
    fun findById(tickerId: String): Ticker
}