package id.walt.gateway.providers.goldorg

import id.walt.gateway.dto.HistoricalPriceData
import id.walt.gateway.dto.HistoricalPriceParameter
import id.walt.gateway.usecases.HistoricalPriceUseCase

class GoldHistoricalPriceUseCaseImpl(
    private val repository: HistoricalPriceRepository,
) : HistoricalPriceUseCase {

    override fun get(parameter: HistoricalPriceParameter): Result<List<HistoricalPriceData>> = runCatching {
        repository.get(parameter.timeframe).map {
            HistoricalPriceData(date = it.key, price = it.value)
        }
    }
}