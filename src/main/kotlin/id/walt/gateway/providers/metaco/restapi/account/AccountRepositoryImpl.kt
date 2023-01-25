package id.walt.gateway.providers.metaco.restapi.account

import id.walt.gateway.CommonHttp
import id.walt.gateway.providers.metaco.repositories.AccountRepository
import id.walt.gateway.providers.metaco.restapi.BaseRestRepository
import id.walt.gateway.providers.metaco.restapi.account.model.Account
import id.walt.gateway.providers.metaco.restapi.services.AuthService

class AccountRepositoryImpl(
    override val authService: AuthService,
) : AccountRepository, BaseRestRepository(authService) {
    private val listEndpoint = "/v1/domains/%s/accounts"
    private val detailEndpoint = "/v1/domains/%s/accounts/%s"

    override fun findAll(domainId: String, criteria: Map<String, String>): List<Account> =
        findAllLoopPages(String.format(CommonHttp.buildUrl(baseUrl, listEndpoint), domainId), criteria)

    override fun findById(domainId: String, accountId: String) = CommonHttp.get<Account>(
        client,
        String.format(
            CommonHttp.buildUrl(baseUrl, detailEndpoint),
            domainId,
            accountId,
        )
    )
}