package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.dto.AssetParameter
import id.walt.gateway.dto.ValueWithChange
import id.walt.gateway.dto.requests.RequestParameter
import id.walt.gateway.dto.requests.RequestResult
import id.walt.gateway.dto.tickers.FeeData
import id.walt.gateway.dto.tickers.TickerData
import id.walt.gateway.dto.tickers.TickerParameter
import id.walt.gateway.dto.tickers.TickerPayloadData
import id.walt.gateway.providers.metaco.CoinMapper.map
import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.providers.metaco.repositories.LedgerRepository
import id.walt.gateway.providers.metaco.repositories.TickerRepository
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.ValidateTickersPayload
import id.walt.gateway.providers.metaco.restapi.ledger.model.fees.Fees
import id.walt.gateway.providers.metaco.restapi.ledger.model.fees.bitcoin.BitcoinFees
import id.walt.gateway.providers.metaco.restapi.ledger.model.fees.ethereum.EthereumFees
import id.walt.gateway.providers.metaco.restapi.models.customproperties.CustomProperties
import id.walt.gateway.providers.metaco.restapi.ticker.model.Ticker
import id.walt.gateway.providers.metaco.restapi.ticker.model.ledgerproperties.ERC20LedgerProperties
import id.walt.gateway.providers.metaco.restapi.ticker.model.ledgerproperties.ERC721LedgerProperties
import id.walt.gateway.providers.metaco.restapi.ticker.model.ledgerproperties.LedgerProperties
import id.walt.gateway.providers.metaco.restapi.ticker.model.ledgerproperties.NativeLedgerProperties
import id.walt.gateway.usecases.CoinUseCase
import id.walt.gateway.usecases.LogoUseCase
import id.walt.gateway.usecases.RequestUseCase
import id.walt.gateway.usecases.TickerUseCase

class TickerUseCaseImpl(
    private val tickerRepository: TickerRepository,
    private val ledgerRepository: LedgerRepository,
    private val coinUseCase: CoinUseCase,
    private val logoUseCase: LogoUseCase,
    private val requestUseCase: RequestUseCase,
) : TickerUseCase {
    override fun get(parameter: TickerParameter): Result<TickerData> = runCatching {
        buildTickerData(tickerRepository.findById(parameter.id), parameter.currency)
    }

    override fun list(currency: String): Result<List<TickerData>> = runCatching {
        tickerRepository.findAll(emptyMap()).filter { !ProviderConfig.tickersIgnore.contains(it.data.id) }.map {
            buildTickerData(it, currency)
        }
    }

    override fun fee(id: String): Result<FeeData> = runCatching {
        tickerRepository.findById(id).data.ledgerId.let {
            getFeeData(ledgerRepository.fees(it))
        }
    }

    override fun validate(id: String): Result<RequestResult> =
        runCatching { tickerRepository.findById(id) }.fold(
            onSuccess = {
                if (checkTickerValidationRequired(it))
                    validateTicker(it)
                else Result.success(
                    RequestResult(result = true, "Validation not required")
                )
            }, onFailure = {
                Result.failure(it)
            })

    private fun checkTickerValidationRequired(ticker: Ticker) = when (getPayloadType(ticker)) {
        Payload.Types.CreateTransferOrder.value -> ticker.signature.isNullOrEmpty()
        else -> false
    }

    private fun getPayloadType(ticker: Ticker) = when (ticker.data.kind) {
        "Contract" -> Payload.Types.CreateTransferOrder.value
        "Native" -> Payload.Types.CreateTransactionOrder.value
        else -> ""
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

    private fun getFeeData(fees: Fees): FeeData = when (fees) {
        is EthereumFees -> FeeData(fee = fees.high.gasPrice, level = "High")
        is BitcoinFees -> FeeData(fee = fees.high.satoshiPerVbyte, level = "High")
        else -> throw IllegalArgumentException("Unknown fees type")
    }

    private fun buildTickerData(ticker: Ticker, currency: String) = coinUseCase.metadata(ticker.map(currency)).fold(
        onSuccess = {
            Pair(
                ValueWithChange(it.askPrice, it.change, currency),
                ValueWithChange(
                    it.bidPrice ?: it.askPrice, it.change, currency
                )
            )
        }, onFailure = {
            Pair(
                ValueWithChange(),
                ValueWithChange()
            )
        }).let {
        TickerData(
            id = ticker.data.id,
            kind = ticker.data.kind,
            chain = ticker.data.ledgerId,
            imageUrl = logoUseCase.get(
                AssetParameter(
                    ticker.data.ledgerId,
                    ticker.data.symbol ?: ticker.data.ledgerDetails.type
                )
            ).data,
            name = ticker.data.name,
            askPrice = it.first,
            bidPrice = it.second,
            decimals = ticker.data.decimals ?: 0,
            symbol = ticker.data.symbol ?: ticker.data.name,
            type = ticker.data.ledgerDetails.type,
            address = extractTickerAddress(ticker.data.ledgerDetails.properties)
        )
    }

    private fun extractTickerAddress(properties: LedgerProperties) = properties.let {
        when (it) {
            is ERC20LedgerProperties -> it.address
            is ERC721LedgerProperties -> it.address
            is NativeLedgerProperties -> null
        }
    }
}