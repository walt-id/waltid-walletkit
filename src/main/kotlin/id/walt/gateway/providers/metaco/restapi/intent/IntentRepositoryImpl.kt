package id.walt.gateway.providers.metaco.restapi.intent

import com.beust.klaxon.Klaxon
import id.walt.gateway.CommonHttp
import id.walt.gateway.providers.metaco.repositories.IntentRepository
import id.walt.gateway.providers.metaco.restapi.AuthService
import id.walt.gateway.providers.metaco.restapi.BaseRestRepository
import id.walt.gateway.providers.metaco.restapi.intent.model.IntentResult
import id.walt.gateway.providers.metaco.restapi.intent.model.ValidationResponse
import id.walt.gateway.providers.metaco.restapi.intent.model.intent.NoSignatureIntent

class IntentRepositoryImpl(
    override val authService: AuthService
) : IntentRepository, BaseRestRepository(authService) {
    private val dryRunEndpoint = "/v1/domains/%s/transactions/dry-run"

    override fun create(domainId: String): IntentResult {
        TODO("Not yet implemented")
    }

    override fun validate(domainId: String, intent: NoSignatureIntent): ValidationResponse = let {
        //TODO: class polymorphism for ktor client serialization doesn't work
//        CommonHttp.post<ValidationResponse>(
//            client,
//            String.format(CommonHttp.buildUrl(baseUrl, dryRunEndpoint), domainId),
//            intent
//        )
        val result = CommonHttp.post(
            client,
            String.format(CommonHttp.buildUrl(baseUrl, dryRunEndpoint), domainId),
            Klaxon().toJsonString(intent)
        )
        Klaxon().parse<ValidationResponse>(result)!!
    }
}