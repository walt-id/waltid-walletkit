package id.walt.gateway.providers.metaco.repositories

import id.walt.gateway.providers.metaco.restapi.ledger.model.Ledger
import id.walt.gateway.providers.metaco.restapi.ledger.model.fees.Fees

interface LedgerRepository {
    fun findAll(criteria: Map<String, String> = emptyMap()): List<Ledger>
    fun findById(id: String): Ledger
    fun fees(id: String): Fees
}