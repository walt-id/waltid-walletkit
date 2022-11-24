package id.walt.gateway.providers.metaco.repositories

import id.walt.gateway.providers.metaco.restapi.transaction.model.Transaction
import id.walt.gateway.providers.metaco.restapi.transaction.model.TransactionList

interface TransactionRepository {
    fun findAll(domainId: String, criteria: Map<String, String>): TransactionList
    fun findById(domainId: String, transactionId: String): Transaction
}