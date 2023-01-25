package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.dto.requests.RequestParameter
import id.walt.gateway.dto.requests.RequestResult
import id.walt.gateway.dto.tickers.TickerData
import id.walt.gateway.dto.tickers.TickerParameter
import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import id.walt.gateway.usecases.RequestUseCase
import id.walt.gateway.usecases.TickerUseCase
import id.walt.gateway.usecases.TradeUseCase

class TradeUseCaseImpl(
    private val tickerUseCase: TickerUseCase,
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

    private fun getPayloadType(ticker: TickerData) = when (ticker.kind) {
        "Contract" -> Payload.Types.CreateTransferOrder.value
        "Native" -> Payload.Types.CreateTransactionOrder.value
        else -> ""
    }

    private fun orderTrade(data: TradeData, dryRun: Boolean = false): Result<RequestResult> =
        runCatching { tickerUseCase.get(TickerParameter(data.trade.ticker)).getOrThrow() }.fold(
            onSuccess = { ticker ->
                if (!dryRun) tickerUseCase.validate(data.trade.ticker)//TODO: check for success and proceed accordingly
                requestUseCase.create(
                    RequestParameter(
                        getPayloadType(ticker),
                        data.trade.sender.domainId,
                        data,
                        ticker.type,
                    )
                )
            },
            onFailure = {
                Result.failure(it)
            }
        )
}
