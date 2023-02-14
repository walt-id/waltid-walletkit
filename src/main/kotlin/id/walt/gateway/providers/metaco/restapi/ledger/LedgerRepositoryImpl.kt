package id.walt.gateway.providers.metaco.restapi.ledger

import id.walt.gateway.CommonHttp
import id.walt.gateway.providers.metaco.repositories.LedgerRepository
import id.walt.gateway.providers.metaco.restapi.BaseRestRepository
import id.walt.gateway.providers.metaco.restapi.ledger.model.Ledger
import id.walt.gateway.providers.metaco.restapi.ledger.model.LedgerList
import id.walt.gateway.providers.metaco.restapi.ledger.model.fees.Fees
import id.walt.gateway.providers.metaco.restapi.services.AuthService

class LedgerRepositoryImpl(
    val auth: AuthService,
) : LedgerRepository, BaseRestRepository(auth) {
    private val listEndpoint = "/v1/ledgers"
    private val detailEndpoint = "/v1/ledgers/%s"
    private val feesEndpoint = "/v1/ledgers/%s/fees"

    override fun findAll(criteria: Map<String, String>): List<Ledger> =
        CommonHttp.get<LedgerList>(
            client,
            CommonHttp.buildUrl(baseUrl, listEndpoint).plus(CommonHttp.buildQueryList(criteria))
        ).items
//        findAllLoopPages<LedgerList, Ledger>(CommonHttp.buildUrl(baseUrl, listEndpoint), criteria)

    override fun findById(id: String): Ledger =
        CommonHttp.get<Ledger>(client, String.format(CommonHttp.buildUrl(baseUrl, detailEndpoint), id))

    override fun fees(id: String): Fees = CommonHttp.get<Fees>(
        client, String.format(
            CommonHttp.buildUrl(baseUrl, feesEndpoint), id
        )
    )
}
