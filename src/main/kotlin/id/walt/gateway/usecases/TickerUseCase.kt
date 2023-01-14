package id.walt.gateway.usecases

import id.walt.gateway.dto.tickers.TickerData
import id.walt.gateway.dto.tickers.TickerParameter

interface TickerUseCase {
    fun get(parameter: TickerParameter): Result<TickerData>
    fun list(currency: String): Result<List<TickerData>>
}