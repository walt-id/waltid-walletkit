package id.walt.gateway.providers.metaco.restapi

import com.beust.klaxon.Klaxon
import id.walt.gateway.dto.intents.IntentBuilderParam
import id.walt.gateway.dto.intents.IntentParameter
import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.dto.trades.TradeResult
import id.walt.gateway.dto.trades.TradeValidationParameter
import id.walt.gateway.dto.users.UserIdentifier
import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.providers.metaco.repositories.IntentRepository
import id.walt.gateway.providers.metaco.repositories.TickerRepository
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
    private val tickerRepository: TickerRepository,
    private val intentSignatureService: SignatureService<NoSignatureIntent>,
) : TradeUseCase {
    override fun sell(spend: TradeData, receive: TradeData): Result<TradeResult> =
        createTransactionOrder("v0_CreateTransactionOrder", spend).also {
            createTransactionOrder("v0_CreateTransactionOrder", receive)
        }

     override fun buy(spend: TradeData, receive: TradeData): Result<TradeResult> =
        createTransactionOrder("v0_CreateTransactionOrder", spend).also {
            createTransactionOrder("v0_CreateTransactionOrder", receive)
        }

    override fun send(parameter: TradeData): Result<TradeResult> =
        createTransactionOrder("v0_CreateTransactionOrder", parameter)

    override fun validate(parameter: TradeValidationParameter): Result<TradeResult> = runCatching {
        //TODO: don't use model here
        TransactionOrderPayload(
            id = UUID.randomUUID().toString(),
            accountId = parameter.transfer.sender.accountId,
            customProperties = CustomProperties(),
            parameters = ParameterBuilder.getBuilder(getTickerType(parameter.transfer.ticker)).build(parameter.transfer)
        ).run {
            intentRepository.validate(parameter.domainId, this).let {
                TradeResult(
                    result = it.result.type == "Success",
                    message = it.result.reason //+ (it.estimate as EthereumEstimate)?.gas.let { " (gas: $it)" }
                )
            }
        }
    }

    private fun getTickerType(tickerId: String) = runCatching {
        tickerRepository.findById(tickerId).data.ledgerDetails.type
    }.fold(onSuccess = {
        it
    }, onFailure = {
        "Unknown"
    })

    private fun createTransactionOrder(type: String, data: TradeData) = runCatching {
        IntentBuilder.getBuilder(IntentBuilderParam(type, getTickerType(data.trade.ticker))).build(
            IntentParameter(data, UserIdentifier(ProviderConfig.domainId, ProviderConfig.userId), "Propose")
        ).let { intent ->
            SignatureIntent(
                request = intent.request,
                signature = Klaxon().parse<SignChallengeResponse>(intentSignatureService.sign(intent as NoSignatureIntent))!!.signature,
            )
        }.run {
            intentRepository.create(this)
        }.let {
            TradeResult(
                result = it.requestId != null,
                message = it.requestId ?: it.message ?: it.reason ?: "Unknown message"
            )
        }
    }
}