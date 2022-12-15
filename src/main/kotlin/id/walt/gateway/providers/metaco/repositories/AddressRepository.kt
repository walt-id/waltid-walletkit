package id.walt.gateway.providers.metaco.repositories

import id.walt.gateway.providers.metaco.restapi.address.model.Address
import id.walt.gateway.providers.metaco.restapi.address.model.AddressList

interface AddressRepository {
    fun findAll(domainId: String, accountId: String): AddressList
    fun findById(domainId: String, accountId: String, addressId: String): Address
}