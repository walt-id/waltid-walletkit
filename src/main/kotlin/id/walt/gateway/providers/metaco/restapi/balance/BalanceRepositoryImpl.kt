package id.walt.gateway.providers.metaco.restapi.balance

import id.walt.gateway.CommonHttp
import id.walt.gateway.providers.metaco.repositories.BalanceRepository
import id.walt.gateway.providers.metaco.restapi.BaseRestRepository
import id.walt.gateway.providers.metaco.restapi.balance.model.Balance
import id.walt.gateway.providers.metaco.restapi.balance.model.BalanceList
import id.walt.gateway.providers.metaco.restapi.services.AuthService

class BalanceRepositoryImpl(
    override val authService: AuthService
) : BalanceRepository, BaseRestRepository(authService) {
    private val endpoint = "/v1/domains/%s/accounts/%s/balances"

    override fun findAll(domainId: String, accountId: String, criteria: Map<String, String>): List<Balance> =
        findAllLoopPages<BalanceList, Balance>(
            String.format(CommonHttp.buildUrl(baseUrl, endpoint), domainId, accountId),
            criteria
        )
}
