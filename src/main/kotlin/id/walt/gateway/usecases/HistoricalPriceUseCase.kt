package id.walt.gateway.usecases

import id.walt.gateway.dto.HistoricalPriceData
import id.walt.gateway.dto.HistoricalPriceParameter

interface HistoricalPriceUseCase {
    fun get(parameter: HistoricalPriceParameter): Result<List<HistoricalPriceData>>
}