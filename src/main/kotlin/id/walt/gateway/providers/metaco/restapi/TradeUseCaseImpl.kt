package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.Common
import id.walt.gateway.dto.QuarantineTransferPayloadData
import id.walt.gateway.dto.accounts.AccountIdentifier
import id.walt.gateway.dto.requests.RequestParameter
import id.walt.gateway.dto.requests.RequestResult
import id.walt.gateway.dto.tickers.TickerData
import id.walt.gateway.dto.tickers.TickerParameter
import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.dto.trades.TransferParameter
import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.providers.metaco.repositories.AddressRepository
import id.walt.gateway.providers.metaco.repositories.TransferRepository
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import id.walt.gateway.providers.metaco.restapi.models.customproperties.toMap
import id.walt.gateway.providers.metaco.restapi.models.destination.AddressDestination
import id.walt.gateway.providers.metaco.restapi.models.destination.Destination
import id.walt.gateway.providers.metaco.restapi.transfer.model.transferparty.AccountTransferParty
import id.walt.gateway.usecases.RequestUseCase
import id.walt.gateway.usecases.TickerUseCase
import id.walt.gateway.usecases.TradeUseCase

class TradeUseCaseImpl(
    private val tickerUseCase: TickerUseCase,
    private val requestUseCase: RequestUseCase,
    private val transferRepository: TransferRepository,
    private val addressRepository: AddressRepository,
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

    override fun validate(parameter: TradeData): Result<RequestResult> =
        tickerUseCase.get(TickerParameter(parameter.trade.ticker)).getOrThrow().let {
            requestUseCase.validate(
                RequestParameter(
                    payloadType = getPayloadType(it),
                    targetDomainId = parameter.trade.sender.domainId,
                    data = parameter,
                    ledgerType = it.type,
                )
            )
        }

    private fun getPayloadType(ticker: TickerData) = when (ticker.kind) {
        "Contract" -> Payload.Types.CreateTransferOrder.value
        "Native" -> Payload.Types.CreateTransactionOrder.value
        else -> ""
    }

    private fun orderTrade(data: TradeData, dryRun: Boolean = false): Result<RequestResult> = let {
        val ticker = tickerUseCase.get(TickerParameter(data.trade.ticker)).getOrThrow()
        if (!dryRun) tickerUseCase.validate(data.trade.ticker)//TODO: check for success and proceed accordingly
        orderReleaseQuarantine(data.trade.sender)
        requestUseCase.create(
            RequestParameter(
                getPayloadType(ticker),
                data.trade.sender.domainId,
                processTradeRecipient(data),
                ticker.type,
            ),
            Common.getTransactionMeta(data.type, data.trade.amount, ticker).toMap()
        )
    }

    private fun processTradeRecipient(trade: TradeData): TradeData =
        Destination.parse(trade.trade.recipient.accountId).takeIf { it is AddressDestination }?.let { trade }
            ?: addressRepository.findAll(trade.trade.recipient.domainId, trade.trade.recipient.accountId, emptyMap())
                .first().address.let {
                    TradeData(
                        TransferParameter(
                            amount = trade.trade.amount,
                            ticker = trade.trade.ticker,
                            maxFee = trade.trade.maxFee,
                            sender = trade.trade.sender,
                            recipient = AccountIdentifier(
                                "",
                                it
                            )
                        ), trade.type
                    )
                }

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
