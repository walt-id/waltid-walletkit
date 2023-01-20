package id.walt.gateway.providers.metaco.restapi

import com.beust.klaxon.Klaxon
import id.walt.gateway.dto.intents.IntentParameter
import id.walt.gateway.dto.intents.PayloadData
import id.walt.gateway.dto.intents.PayloadParameter
import id.walt.gateway.dto.tickers.TickerPayloadData
import id.walt.gateway.dto.trades.OrderResult
import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.dto.trades.TradeValidationParameter
import id.walt.gateway.dto.users.UserIdentifier
import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.providers.metaco.repositories.IntentRepository
import id.walt.gateway.providers.metaco.repositories.TickerRepository
import id.walt.gateway.providers.metaco.restapi.intent.builders.IntentBuilder
import id.walt.gateway.providers.metaco.restapi.intent.builders.parameters.ParameterBuilder
import id.walt.gateway.providers.metaco.restapi.intent.builders.payload.PayloadBuilder
import id.walt.gateway.providers.metaco.restapi.intent.model.NoSignatureIntent
import id.walt.gateway.providers.metaco.restapi.intent.model.SignatureIntent
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.TransactionOrderPayload
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.ValidateTickersPayload
import id.walt.gateway.providers.metaco.restapi.models.customproperties.CustomProperties
import id.walt.gateway.providers.metaco.restapi.services.SignChallengeResponse
import id.walt.gateway.providers.metaco.restapi.services.SignatureService
import id.walt.gateway.providers.metaco.restapi.ticker.model.Ticker
import id.walt.gateway.usecases.TradeUseCase
import java.util.*

class TradeUseCaseImpl(
    private val intentRepository: IntentRepository,
    private val tickerRepository: TickerRepository,
    private val intentSignatureService: SignatureService<NoSignatureIntent>,
) : TradeUseCase {
    override fun sell(spend: TradeData, receive: TradeData): Result<OrderResult> =
        orderTrade(spend).also {
            orderTrade(receive)
        }

    override fun buy(spend: TradeData, receive: TradeData): Result<OrderResult> =
        orderTrade(spend).also {
            orderTrade(receive)
        }

    override fun send(parameter: TradeData): Result<OrderResult> = orderTrade(parameter)

    override fun validate(parameter: TradeValidationParameter): Result<OrderResult> = runCatching {
        //TODO: don't use model here
        TransactionOrderPayload(
            id = UUID.randomUUID().toString(),
            accountId = parameter.transfer.sender.accountId,
            customProperties = CustomProperties(),
            parameters = ParameterBuilder.getBuilder(tickerRepository.findById(parameter.transfer.ticker).data.ledgerDetails.type)
                .build(parameter.transfer)
        ).run {
            intentRepository.validate(parameter.domainId, this).let {
                OrderResult(
                    result = it.result.type == "Success",
                    message = it.result.reason //+ (it.estimate as EthereumEstimate)?.gas.let { " (gas: $it)" }
                )
            }
        }
    }

    private fun getPayloadType(ticker: Ticker) = when (ticker.data.kind) {
        "Contract" -> Payload.Types.CreateTransferOrder.value
        "Native" -> Payload.Types.CreateTransactionOrder.value
        else -> ""
    }

    private fun checkTickerValidationRequired(ticker: Ticker) = when (getPayloadType(ticker)) {
        Payload.Types.CreateTransferOrder.value -> ticker.signature.isNullOrEmpty()
        else -> false
    }

    private fun validateTicker(ticker: Ticker) = createIntentRequest(
        Payload.Types.ValidateTickers.value, ProviderConfig.domainId, TickerPayloadData(
            ticker = ticker.fromTicker()
        )
    )

    private fun Ticker.fromTicker() = ValidateTickersPayload.TickerData(
        id = this.data.id,
        ledgerId = this.data.ledgerId,
        kind = this.data.kind,
        name = this.data.name,
        decimals = this.data.decimals,
        symbol = this.data.symbol,
        ledgerDetails = this.data.ledgerDetails,
        lock = this.data.lock,
        customProperties = CustomProperties()
    )

    private fun orderTrade(data: TradeData): Result<OrderResult> =
        tickerRepository.findById(data.trade.ticker).let { ticker ->
            if (checkTickerValidationRequired(ticker)) {
                validateTicker(ticker)
            }//TODO: check for success and proceed accordingly
            createIntentRequest(
                getPayloadType(ticker),
                data.trade.sender.domainId,
                data,
                ticker.data.ledgerDetails.type,
            )
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
            OrderResult(
                result = it.requestId != null,
                message = it.requestId ?: it.message ?: it.reason ?: "Unknown message"
            )
        }
    }
}
