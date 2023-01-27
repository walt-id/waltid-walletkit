package id.walt.gateway.providers.metaco.restapi.transaction

import id.walt.gateway.CommonHttp
import id.walt.gateway.providers.metaco.repositories.TransactionRepository
import id.walt.gateway.providers.metaco.restapi.BaseRestRepository
import id.walt.gateway.providers.metaco.restapi.services.AuthService
import id.walt.gateway.providers.metaco.restapi.transaction.model.Transaction
import id.walt.gateway.providers.metaco.restapi.transaction.model.TransactionList

class TransactionRepositoryImpl(
    override val authService: AuthService
) : TransactionRepository, BaseRestRepository(authService) {
    private val listEndpoint = "/v1/domains/%s/transactions"
    private val detailEndpoint = "/v1/domains/%s/transactions/%s"

    override fun findAll(domainId: String, criteria: Map<String, String>): List<Transaction> =
        findAllLoopPages<TransactionList, Transaction>(
            String.format(
                CommonHttp.buildUrl(baseUrl, listEndpoint),
                domainId
            ), criteria
        )

    override fun findById(domainId: String, transactionId: String) = CommonHttp.get<Transaction>(
        client,
        String.format(
            CommonHttp.buildUrl(baseUrl, detailEndpoint),
            domainId,
            transactionId,
        )
    )
}