package id.walt.gateway.providers.metaco.restapi

import com.beust.klaxon.Klaxon
import id.walt.gateway.dto.IntentBuilderParam
import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.dto.trades.TradeResult
import id.walt.gateway.dto.trades.TradeValidationParameter
import id.walt.gateway.providers.metaco.repositories.IntentRepository
import id.walt.gateway.providers.metaco.restapi.intent.builders.IntentBuilder
import id.walt.gateway.providers.metaco.restapi.intent.builders.ParameterBuilder
import id.walt.gateway.providers.metaco.restapi.intent.model.NoSignatureIntent
import id.walt.gateway.providers.metaco.restapi.intent.model.SignatureIntent
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.TransactionOrderPayload
import id.walt.gateway.providers.metaco.restapi.models.customproperties.CustomProperties
import id.walt.gateway.providers.metaco.restapi.services.SignChallengeResponse
import id.walt.gateway.providers.metaco.restapi.services.SignatureService
import id.walt.gateway.usecases.TradeUseCase
import java.util.*

class TradeUseCaseImpl(
    private val intentRepository: IntentRepository,
    private val intentSignatureService: SignatureService<NoSignatureIntent>,
) : TradeUseCase {
    override fun sell(parameter: TradeData): Result<TradeResult> =
        createTransactionOrder("v0_CreateTransactionOrder", parameter)

    override fun buy(parameter: TradeData): Result<TradeResult> =
        createTransactionOrder("v0_CreateTransactionOrder", parameter)

    override fun send(parameter: TradeData): Result<TradeResult> =
        //TODO: v0_CreateTransferOrder?
        createTransactionOrder("v0_CreateTransactionOrder", parameter)

    override fun validate(parameter: TradeValidationParameter): Result<TradeResult> = runCatching {
        //TODO: don't use model here
        TransactionOrderPayload(
            id = UUID.randomUUID().toString(),
            accountId = parameter.trade.sender,
            customProperties = CustomProperties(),
            parameters = ParameterBuilder.getBuilder(parameter.trade.ticker).build(parameter.trade)
        ).run {
            intentRepository.validate(parameter.domainId, this).let {
                TradeResult(
                    result = it.result.type == "Success",
                    message = it.result.reason //+ (it.estimate as EthereumEstimate)?.gas.let { " (gas: $it)" }
                )
            }
        }
    }

    private fun createTransactionOrder(type: String, data: TradeData) = runCatching {
        IntentBuilder.getBuilder(IntentBuilderParam(type, data.trade.ticker)).build(data)
            .let { intent ->
                SignatureIntent(
                    request = intent.request,
                    signature = Klaxon().parse<SignChallengeResponse>(intentSignatureService.sign(intent as NoSignatureIntent))!!.signature,
                )
            }.run {
                intentRepository.create(data.domainId, this)
            }.let {
                TradeResult(
                    result = it.requestId != null,
                    message = it.requestId ?: it.message ?: it.reason ?: "Unknown error"
                )
            }
    }
}