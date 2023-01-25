package id.walt.gateway.providers.metaco.repositories

import id.walt.gateway.providers.metaco.restapi.transfer.model.Transfer

interface TransferRepository {
    fun findAll(domainId: String, criteria: Map<String, String>): List<Transfer>
    fun findById(domainId: String, transferId: String): Transfer
}