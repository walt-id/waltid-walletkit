package id.walt.gateway.providers.metaco.repositories

import id.walt.gateway.providers.metaco.restapi.address.model.Address

interface AddressRepository {
    fun findAll(domainId: String, accountId: String, criteria: Map<String, String>): List<Address>
    fun findById(domainId: String, accountId: String, addressId: String): Address
}