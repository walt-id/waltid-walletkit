package id.walt.gateway.providers.metaco.repositories

import id.walt.gateway.providers.metaco.restapi.domain.model.Domain

interface DomainRepository {
    fun findAll(criteria: Map<String, String>): List<Domain>
    fun findById(domainId: String): Domain
}