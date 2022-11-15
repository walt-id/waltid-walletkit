package id.walt.gateway.providers.metaco.repositories

import id.walt.gateway.providers.metaco.restapi.account.model.Account
import id.walt.gateway.providers.metaco.restapi.account.model.AccountList

interface AccountRepository {
    fun findAll(domainId: String, criteria: Map<String, String>): AccountList
    fun findById(domainId: String, accountId: String): Account
}