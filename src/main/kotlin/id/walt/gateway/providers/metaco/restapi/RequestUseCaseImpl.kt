package id.walt.gateway.providers.metaco.restapi

import com.beust.klaxon.Klaxon
import id.walt.gateway.dto.intents.IntentParameter
import id.walt.gateway.dto.intents.PayloadData
import id.walt.gateway.dto.intents.PayloadParameter
import id.walt.gateway.dto.requests.RequestParameter
import id.walt.gateway.dto.requests.RequestResult
import id.walt.gateway.dto.users.UserIdentifier
import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.providers.metaco.repositories.IntentRepository
import id.walt.gateway.providers.metaco.restapi.intent.builders.IntentBuilder
import id.walt.gateway.providers.metaco.restapi.intent.builders.payload.PayloadBuilder
import id.walt.gateway.providers.metaco.restapi.intent.model.NoSignatureIntent
import id.walt.gateway.providers.metaco.restapi.intent.model.SignatureIntent
import id.walt.gateway.providers.metaco.restapi.services.SignChallengeResponse
import id.walt.gateway.providers.metaco.restapi.services.SignatureService
import id.walt.gateway.usecases.RequestUseCase

class RequestUseCaseImpl(
    private val intentRepository: IntentRepository,
    private val intentSignatureService: SignatureService<NoSignatureIntent>,
) : RequestUseCase {

    override fun create(parameter: RequestParameter): Result<RequestResult> =
        createIntentRequest(parameter.payloadType, parameter.targetDomainId, parameter.data, parameter.ledgerType)

    override fun validate(parameter: RequestParameter): Result<RequestResult> = runCatching {
        intentRepository.validate(
            parameter.targetDomainId, PayloadBuilder.create(
                PayloadParameter(
                    type = parameter.payloadType,
                    parametersType = parameter.ledgerType,
                    data = parameter.data,
                )
            )
        ).let {
            RequestResult(
                result = it.result.type == "Success",
                message = it.result.reason //+ (it.estimate as EthereumEstimate)?.gas.let { " (gas: $it)" }
            )
        }
    }

    private fun <T : PayloadData> createIntentRequest(
        payloadType: String,
        targetDomainId: String,
        data: T,
        ledgerType: String? = null
    ) = runCatching {
        IntentBuilder.build(
            IntentParameter(
                targetDomainId = targetDomainId,
                author = UserIdentifier(ProviderConfig.domainId, ProviderConfig.userId),
                type = "Propose",
            ), PayloadBuilder.create(
                PayloadParameter(
                    type = payloadType,
                    parametersType = ledgerType,
                    data = data,
                )
            )
        ).let { intent ->
            SignatureIntent(
                request = intent.request,
                signature = Klaxon().parse<SignChallengeResponse>(intentSignatureService.sign(intent as NoSignatureIntent))!!.signature,
            )
        }.run {
            intentRepository.create(this)
        }.let {
            RequestResult(
                result = it.requestId != null,
                message = it.requestId ?: it.message ?: it.reason ?: "Unknown message"
            )
        }
    }
}