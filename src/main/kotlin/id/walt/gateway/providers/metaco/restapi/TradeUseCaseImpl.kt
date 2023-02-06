package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.Common
import id.walt.gateway.dto.QuarantineTransferPayloadData
import id.walt.gateway.dto.accounts.AccountIdentifier
import id.walt.gateway.dto.requests.RequestParameter
import id.walt.gateway.dto.requests.RequestResult
import id.walt.gateway.dto.tickers.TickerData
import id.walt.gateway.dto.tickers.TickerParameter
import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.providers.metaco.repositories.TransferRepository
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import id.walt.gateway.providers.metaco.restapi.transfer.model.transferparty.AccountTransferParty
import id.walt.gateway.usecases.RequestUseCase
import id.walt.gateway.usecases.TickerUseCase
import id.walt.gateway.usecases.TradeUseCase

class TradeUseCaseImpl(
    private val tickerUseCase: TickerUseCase,
    private val requestUseCase: RequestUseCase,
    private val transferRepository: TransferRepository,
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

    override fun validate(parameter: TradeData): Result<RequestResult> = requestUseCase.validate(
        RequestParameter(
            payloadType = Payload.Types.CreateTransactionOrder.value,
            targetDomainId = parameter.trade.sender.domainId,
            data = parameter,
            ledgerType = tickerUseCase.get(TickerParameter(parameter.trade.ticker)).getOrThrow().type
        )
    )

    private fun getPayloadType(ticker: TickerData) = when (ticker.kind) {
        "Contract" -> Payload.Types.CreateTransferOrder.value
        "Native" -> Payload.Types.CreateTransactionOrder.value
        else -> ""
    }

    private fun orderTrade(data: TradeData, dryRun: Boolean = false): Result<RequestResult> =
        runCatching { tickerUseCase.get(TickerParameter(data.trade.ticker)).getOrThrow() }.fold(
            onSuccess = { ticker ->
                if (!dryRun) tickerUseCase.validate(data.trade.ticker)//TODO: check for success and proceed accordingly
                orderReleaseQuarantine(data.trade.sender)
                requestUseCase.create(
                    RequestParameter(
                        getPayloadType(ticker),
                        data.trade.sender.domainId,
                        data,
                        ticker.type,
                    ),
                    mapOf(
                        "value" to (Common.computeAmount(data.trade.amount, ticker.decimals) * ticker.price.value).toString(),
                        "change" to (Common.computeAmount(data.trade.amount, ticker.decimals) * ticker.price.change).toString(),
                        "currency" to ticker.price.currency
                    )
                )
            },
            onFailure = {
                Result.failure(it)
            }
        )

    private fun orderReleaseQuarantine(account: AccountIdentifier) = runCatching {
        transferRepository.findAll(account.domainId, mapOf("accountId" to account.accountId, "quarantined" to "true"))
            .filter {
                Common.computeAmount(
                    it.value, tickerUseCase.get(TickerParameter(it.tickerId)).getOrNull()?.decimals ?: 0
                ) <= ProviderConfig.preApprovedTransferAmount.toDouble() && it.senders.none {
                    (it as? AccountTransferParty)?.accountId == account.accountId
                }
            }.takeIf {
                it.isNotEmpty()
            }?.let {
                requestUseCase.create(
                    RequestParameter(
                        payloadType = Payload.Types.ReleaseQuarantinedTransfers.value,
                        targetDomainId = account.domainId,
                        data = QuarantineTransferPayloadData(
                            accountId = account.accountId,
                            transfers = it.map { it.id },
                        )
                    )
                )
            } ?: Result.success(RequestResult(true, "Nothing to release from quarantine"))
    }
}
