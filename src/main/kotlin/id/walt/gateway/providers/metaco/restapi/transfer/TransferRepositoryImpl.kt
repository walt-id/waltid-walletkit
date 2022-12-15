package id.walt.gateway.providers.metaco.restapi.transfer

import id.walt.gateway.providers.metaco.repositories.TransferRepository
import id.walt.gateway.providers.metaco.restapi.services.AuthService
import id.walt.gateway.providers.metaco.restapi.BaseRestRepository
import id.walt.gateway.CommonHttp
import id.walt.gateway.providers.metaco.restapi.transfer.model.Transfer
import id.walt.gateway.providers.metaco.restapi.transfer.model.TransferList

class TransferRepositoryImpl(
    override val authService: AuthService
): TransferRepository, BaseRestRepository(authService) {
    private val listEndpoint = "/v1/domains/%s/transactions/transfers%s"//?transactionId=fe8da9e8-520a-497c-9001-0b26c8067d3b"
    private val detailEndpoint = "/v1/domains/%s/transactions/transfers/%s"

    override fun findAll(domainId: String, criteria: Map<String, String>) = CommonHttp.get<TransferList>(
        client,
        String.format(
            CommonHttp.buildUrl(baseUrl, listEndpoint),
            domainId,
            CommonHttp.buildQueryList(criteria)
        )
    )

    override fun findById(domainId: String, transferId: String) = CommonHttp.get<Transfer>(
        client,
        String.format(
            CommonHttp.buildUrl(baseUrl, detailEndpoint),
            domainId,
            transferId,
        )
    )
}