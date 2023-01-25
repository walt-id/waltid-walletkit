package id.walt.gateway.providers.metaco.restapi.domain

import id.walt.gateway.CommonHttp
import id.walt.gateway.providers.metaco.repositories.DomainRepository
import id.walt.gateway.providers.metaco.restapi.BaseRestRepository
import id.walt.gateway.providers.metaco.restapi.domain.model.Domain
import id.walt.gateway.providers.metaco.restapi.services.AuthService

class DomainRepositoryImpl(
    override val authService: AuthService,
) : DomainRepository, BaseRestRepository(authService) {
    private val listEndpoint = "/v1/domains"
    private val detailEndpoint = "/v1/domains/%s"

    override fun findAll(criteria: Map<String, String>): List<Domain> =
        findAllLoopPages(CommonHttp.buildUrl(baseUrl, listEndpoint), criteria)

    override fun findById(domainId: String): Domain = CommonHttp.get<Domain>(
        client,
        String.format(
            CommonHttp.buildUrl(baseUrl, detailEndpoint),
            domainId,
        )
    )
}