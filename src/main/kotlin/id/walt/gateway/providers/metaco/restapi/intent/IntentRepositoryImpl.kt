package id.walt.gateway.providers.metaco.restapi.intent

import com.beust.klaxon.Klaxon
import id.walt.gateway.CommonHttp
import id.walt.gateway.providers.metaco.repositories.IntentRepository
import id.walt.gateway.providers.metaco.restapi.AuthService
import id.walt.gateway.providers.metaco.restapi.BaseRestRepository
import id.walt.gateway.providers.metaco.restapi.intent.model.IntentResult
import id.walt.gateway.providers.metaco.restapi.intent.model.ValidationResponse
import id.walt.gateway.providers.metaco.restapi.intent.model.NoSignatureIntent
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import id.walt.gateway.providers.metaco.restapi.signservice.SignatureService

class IntentRepositoryImpl(
    override val authService: AuthService,
    val intentSignatureService: SignatureService<NoSignatureIntent>
) : IntentRepository, BaseRestRepository(authService) {
    private val dryRunEndpoint = "/v1/domains/%s/transactions/dry-run"
    private val createEndpoint = "/v1/intents"

    override fun create(domainId: String): IntentResult {
        TODO("Not yet implemented")
    }

    override fun validate(domainId: String, payload: Payload): ValidationResponse = let {
        //TODO: class polymorphism for ktor client serialization doesn't work
//        CommonHttp.post<ValidationResponse>(
//            client,
//            String.format(CommonHttp.buildUrl(baseUrl, dryRunEndpoint), domainId),
//            intent
//        )
        val result = CommonHttp.post(
            client,
            String.format(CommonHttp.buildUrl(baseUrl, dryRunEndpoint), domainId),
            Klaxon().toJsonString(payload)
        )
        Klaxon().parse<ValidationResponse>(result)!!
    }
}