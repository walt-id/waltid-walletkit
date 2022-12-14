package id.walt.gateway.providers.metaco.restapi.address

import id.walt.gateway.CommonHttp
import id.walt.gateway.providers.metaco.repositories.AddressRepository
import id.walt.gateway.providers.metaco.restapi.AuthService
import id.walt.gateway.providers.metaco.restapi.BaseRestRepository
import id.walt.gateway.providers.metaco.restapi.address.model.Address
import id.walt.gateway.providers.metaco.restapi.address.model.AddressList

class AddressRepositoryImpl(
    override val authService: AuthService,
) : AddressRepository, BaseRestRepository(authService) {
    private val listEndpoint = "/v1/domains/%s/accounts/%s/addresses"
    private val detailEndpoint = "/v1/domains/%s/accounts/%s/addresses/%s"

    override fun findAll(domainId: String, accountId: String): AddressList = CommonHttp.get<AddressList>(
        client,
        String.format(
            CommonHttp.buildUrl(baseUrl, listEndpoint),
            domainId,
            accountId
        )
    )

    override fun findById(domainId: String, accountId: String, addressId: String): Address = CommonHttp.get<Address>(
        client,
        String.format(
            detailEndpoint,
            domainId,
            accountId,
            addressId
        )
    )
}