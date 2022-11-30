package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.dto.AssetParameter
import id.walt.gateway.dto.TickerData
import id.walt.gateway.dto.TickerParameter
import id.walt.gateway.dto.ValueWithChange
import id.walt.gateway.providers.metaco.CoinMapper.map
import id.walt.gateway.providers.metaco.repositories.TickerRepository
import id.walt.gateway.usecases.CoinUseCase
import id.walt.gateway.usecases.LogoUseCase
import id.walt.gateway.usecases.TickerUseCase

class TickerUseCaseImpl(
    private val tickerRepository: TickerRepository,
    private val coinUseCase: CoinUseCase,
    private val logoUseCase: LogoUseCase,
) : TickerUseCase {
    override fun get(parameter: TickerParameter): Result<TickerData> = runCatching {
        tickerRepository.findById(parameter.id).let {
            TickerData(
                id = it.data.id,
                kind = it.data.kind,
                chain = it.data.ledgerId,
                imageUrl = logoUseCase.get(AssetParameter(it.data.name, it.data.symbol)).data,
                name = it.data.name,
                price = coinUseCase.metadata(it.map(parameter.currency)).fold(
                    onSuccess = {
                        ValueWithChange(it.price, it.change, parameter.currency)
                    }, onFailure = {
                        ValueWithChange()
                    }),
                decimals = it.data.decimals,
                symbol = it.data.symbol,
            )
        }
    }
}