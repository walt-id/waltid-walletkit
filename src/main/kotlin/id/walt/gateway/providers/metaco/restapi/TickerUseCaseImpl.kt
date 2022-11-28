package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.dto.TickerData
import id.walt.gateway.dto.TickerParameter
import id.walt.gateway.dto.ValueWithChange
import id.walt.gateway.providers.metaco.CoinMapper.map
import id.walt.gateway.providers.metaco.repositories.TickerRepository
import id.walt.gateway.usecases.CoinUseCase
import id.walt.gateway.usecases.TickerUseCase

class TickerUseCaseImpl(
    private val tickerRepository: TickerRepository,
    private val coinUseCase: CoinUseCase,
) : TickerUseCase {
    override fun get(parameter: TickerParameter): Result<TickerData> = runCatching {
        tickerRepository.findById(parameter.id).let {
            TickerData(
                imageUrl = "TBD",
                name = it.data.name,
                price = coinUseCase.metadata(it.map()).fold(
                    onSuccess = {
                        ValueWithChange(it.price, it.change)
                    }, onFailure = {
                        ValueWithChange()
                    })
            )
        }
    }
}