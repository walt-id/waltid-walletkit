package id.walt.gateway.providers.metaco.restapi.intent

import com.beust.klaxon.Klaxon
import id.walt.gateway.CommonHttp
import id.walt.gateway.providers.metaco.repositories.IntentRepository
import id.walt.gateway.providers.metaco.restapi.BaseRestRepository
import id.walt.gateway.providers.metaco.restapi.intent.model.Intent
import id.walt.gateway.providers.metaco.restapi.intent.model.SignatureIntent
import id.walt.gateway.providers.metaco.restapi.intent.model.result.IntentResult
import id.walt.gateway.providers.metaco.restapi.intent.model.validation.ValidationResult
import id.walt.gateway.providers.metaco.restapi.services.AuthService

class IntentRepositoryImpl(
    override val authService: AuthService,
) : IntentRepository, BaseRestRepository(authService) {
    private val dryRunEndpoint = "/v1/intents/dry-run"
    private val createEndpoint = "/v1/intents"

    override fun create(intent: SignatureIntent): IntentResult = let {
        val result = CommonHttp.post(
            client,
            String.format(CommonHttp.buildUrl(baseUrl, createEndpoint)),
            Klaxon().toJsonString(intent)
        )
        Klaxon().parse<IntentResult>(result)!!
    }

    override fun validate(intent: Intent): ValidationResult = let {
        //TODO: class polymorphism for ktor client serialization doesn't work
//        CommonHttp.post<ValidationResponse>(
//            client,
//            String.format(CommonHttp.buildUrl(baseUrl, dryRunEndpoint), domainId),
//            intent
//        )
        val result = CommonHttp.post(
            client,
            CommonHttp.buildUrl(baseUrl, dryRunEndpoint),
            Klaxon().toJsonString(intent.request)
        )
        Klaxon().parse<ValidationResult>(result)!!
    }
}