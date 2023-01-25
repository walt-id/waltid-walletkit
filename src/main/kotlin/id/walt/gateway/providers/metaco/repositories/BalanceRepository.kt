package id.walt.gateway.providers.metaco.repositories

import id.walt.gateway.providers.metaco.restapi.balance.model.Balance

interface BalanceRepository {
    fun findAll(domainId: String, accountId: String, criteria: Map<String, String>): List<Balance>
}