package id.walt.gateway.providers.metaco.repositories

import id.walt.gateway.providers.metaco.restapi.account.model.Account

interface AccountRepository {
    fun findAll(domainId: String, criteria: Map<String, String>): List<Account>
    fun findById(domainId: String, accountId: String): Account
}