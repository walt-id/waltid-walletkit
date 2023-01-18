package id.walt.gateway.providers.metaco.repositories

import id.walt.gateway.providers.metaco.restapi.domain.model.Domain
import id.walt.gateway.providers.metaco.restapi.domain.model.DomainList

interface DomainRepository {
    fun findAll(criteria: Map<String, String>): DomainList
    fun findById(domainId: String): Domain
}