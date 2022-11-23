package id.walt.gateway.providers.metaco.restapi.balance

import id.walt.gateway.providers.metaco.repositories.BalanceRepository
import id.walt.gateway.providers.metaco.restapi.AuthService
import id.walt.gateway.providers.metaco.restapi.BaseRestRepository
import id.walt.gateway.providers.metaco.restapi.CommonHttp
import id.walt.gateway.providers.metaco.restapi.balance.model.BalanceList

class BalanceRepositoryImpl(
    override val authService: AuthService
) : BalanceRepository, BaseRestRepository(authService) {
    private val endpoint = "/v1/domains/%s/accounts/%s/balances"

    override fun findAll(domainId: String, accountId: String, criteria: Map<String, String>) =
        CommonHttp.get<BalanceList>(
            client,
            String.format(
                CommonHttp.buildUrl(baseUrl, endpoint),
                domainId,
                accountId,
                CommonHttp.buildQueryList(criteria)
            )
        )
}