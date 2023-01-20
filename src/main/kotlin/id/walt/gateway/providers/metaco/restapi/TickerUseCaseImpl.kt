package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.dto.AssetParameter
import id.walt.gateway.dto.tickers.TickerData
import id.walt.gateway.dto.tickers.TickerParameter
import id.walt.gateway.dto.ValueWithChange
import id.walt.gateway.providers.metaco.CoinMapper.map
import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.providers.metaco.repositories.TickerRepository
import id.walt.gateway.providers.metaco.restapi.ticker.model.Ticker
import id.walt.gateway.providers.metaco.restapi.ticker.model.ledgerproperties.ERC20LedgerProperties
import id.walt.gateway.providers.metaco.restapi.ticker.model.ledgerproperties.ERC721LedgerProperties
import id.walt.gateway.providers.metaco.restapi.ticker.model.ledgerproperties.LedgerProperties
import id.walt.gateway.providers.metaco.restapi.ticker.model.ledgerproperties.NativeLedgerProperties
import id.walt.gateway.usecases.CoinUseCase
import id.walt.gateway.usecases.LogoUseCase
import id.walt.gateway.usecases.TickerUseCase

class TickerUseCaseImpl(
    private val tickerRepository: TickerRepository,
    private val coinUseCase: CoinUseCase,
    private val logoUseCase: LogoUseCase,
) : TickerUseCase {
    override fun get(parameter: TickerParameter): Result<TickerData> = runCatching {
        buildTickerData(tickerRepository.findById(parameter.id), parameter.currency)
    }

    override fun list(currency: String): Result<List<TickerData>> = runCatching {
        tickerRepository.findAll(emptyMap()).items.filter { !ProviderConfig.tickersIgnore.contains(it.data.id) }.map {
            buildTickerData(it, currency)
        }
    }

    private fun buildTickerData(ticker: Ticker, currency: String) = TickerData(
        id = ticker.data.id,
        kind = ticker.data.kind,
        chain = ticker.data.ledgerId,
        imageUrl = logoUseCase.get(AssetParameter(ticker.data.ledgerId, ticker.data.symbol?:ticker.data.ledgerDetails.type)).data,
        name = ticker.data.name,
        price = coinUseCase.metadata(ticker.map(currency)).fold(
            onSuccess = {
                ValueWithChange(it.price, it.change, currency)
            }, onFailure = {
                ValueWithChange()
            }),
        decimals = ticker.data.decimals ?: 0,
        symbol = ticker.data.symbol ?: ticker.data.name,
        maxFee = 50250000462000,
        address = extractTickerAddress(ticker.data.ledgerDetails.properties)
    )

    private fun extractTickerAddress(properties: LedgerProperties) = properties.let {
        when (it) {
            is ERC20LedgerProperties -> it.address
            is ERC721LedgerProperties -> it.address
            is NativeLedgerProperties -> null
        }
    }
}