package id.walt.gateway.providers.metaco.repositories

import id.walt.gateway.providers.metaco.restapi.transaction.model.Transaction

interface TransactionRepository {
    fun findAll(domainId: String, criteria: Map<String, String>): List<Transaction>
    fun findById(domainId: String, transactionId: String): Transaction
}