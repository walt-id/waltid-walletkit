package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.dto.requests.RequestParameter
import id.walt.gateway.dto.requests.RequestResult
import id.walt.gateway.dto.tickers.TickerPayloadData
import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.providers.metaco.repositories.TickerRepository
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.ValidateTickersPayload
import id.walt.gateway.providers.metaco.restapi.models.customproperties.CustomProperties
import id.walt.gateway.providers.metaco.restapi.ticker.model.Ticker
import id.walt.gateway.usecases.RequestUseCase
import id.walt.gateway.usecases.TradeUseCase

class TradeUseCaseImpl(
    private val tickerRepository: TickerRepository,
    private val requestUseCase: RequestUseCase,
) : TradeUseCase {
    override fun sell(spend: TradeData, receive: TradeData): Result<RequestResult> =
        orderTrade(spend).also {
            orderTrade(receive)
        }

    override fun buy(spend: TradeData, receive: TradeData): Result<RequestResult> =
        orderTrade(spend).also {
            orderTrade(receive)
        }

    override fun send(send: TradeData): Result<RequestResult> = orderTrade(send)

    override fun validate(parameter: TradeData): Result<RequestResult> = orderTrade(parameter, true)

    private fun getPayloadType(ticker: Ticker) = when (ticker.data.kind) {
        "Contract" -> Payload.Types.CreateTransferOrder.value
        "Native" -> Payload.Types.CreateTransactionOrder.value
        else -> ""
    }

    private fun checkTickerValidationRequired(ticker: Ticker) = when (getPayloadType(ticker)) {
        Payload.Types.CreateTransferOrder.value -> ticker.signature.isNullOrEmpty()
        else -> false
    }

    private fun validateTicker(ticker: Ticker) = requestUseCase.create(
        RequestParameter(
            Payload.Types.ValidateTickers.value,
            ProviderConfig.domainId,
            TickerPayloadData(ticker = ticker.fromTicker())
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

    private fun orderTrade(data: TradeData, dryRun: Boolean = false): Result<RequestResult> =
        runCatching { tickerRepository.findById(data.trade.ticker) }.fold(
            onSuccess = { ticker ->
                if (!dryRun && checkTickerValidationRequired(ticker)) validateTicker(ticker)//TODO: check for success and proceed accordingly
                requestUseCase.create(
                    RequestParameter(
                        getPayloadType(ticker),
                        data.trade.sender.domainId,
                        data,
                        ticker.data.ledgerDetails.type,
                    )
                )
            },
            onFailure = {
                Result.failure(it)
            }
        )
}
