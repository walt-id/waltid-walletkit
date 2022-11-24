package id.walt.gateway.providers.metaco.repositories

import id.walt.gateway.providers.metaco.restapi.transfer.model.Transfer
import id.walt.gateway.providers.metaco.restapi.transfer.model.TransferList

interface TransferRepository {
    fun findAll(domainId: String, criteria: Map<String, String>): TransferList
    fun findById(domainId: String, transferId: String): Transfer
}