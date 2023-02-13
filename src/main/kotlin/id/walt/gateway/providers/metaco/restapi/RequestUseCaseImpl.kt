package id.walt.gateway.providers.metaco.restapi

import com.beust.klaxon.Klaxon
import id.walt.gateway.dto.intents.IntentParameter
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

    override fun create(parameter: RequestParameter, additionalInfo: Map<String, String>): Result<RequestResult> =
        runCatching {
            createIntent(parameter.payloadType, parameter, additionalInfo, "Propose").let { intent ->
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

    override fun validate(parameter: RequestParameter): Result<RequestResult> = runCatching {
        createIntent(parameter.payloadType, parameter, emptyMap()).let { intent ->
            intentRepository.validate(intent)
        }.let {
//            RequestResult(
//                result = it.result.type == "Success",
//                message = it.result.reason //+ (it.estimate as EthereumEstimate)?.gas.let { " (gas: $it)" }
//            )
            RequestResult(
                result = it.success,
                message = it.errors?.joinToString(".").takeIf { it?.isNotEmpty() ?: false } ?: "Unknown error"
            )
        }
    }

    private fun createIntent(
        payloadType: String,
        parameter: RequestParameter,
        additionalInfo: Map<String, String>,
        intentType: String? = null,
    ) = IntentBuilder.build(
        IntentParameter(
            targetDomainId = parameter.targetDomainId,
            author = UserIdentifier(ProviderConfig.domainId, ProviderConfig.userId),
            type = intentType,
        ), PayloadBuilder.create(
            PayloadParameter(
                type = payloadType,
                data = parameter.data,
                parametersType = parameter.ledgerType ?: "",
                additionalInfo = additionalInfo,
            )
        )
    )
}