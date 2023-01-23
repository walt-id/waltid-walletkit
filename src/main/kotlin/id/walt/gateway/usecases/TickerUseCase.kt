package id.walt.gateway.usecases

import id.walt.gateway.dto.requests.RequestResult
import id.walt.gateway.dto.tickers.FeeData
import id.walt.gateway.dto.tickers.TickerData
import id.walt.gateway.dto.tickers.TickerParameter

interface TickerUseCase {
    fun get(parameter: TickerParameter): Result<TickerData>
    fun list(currency: String): Result<List<TickerData>>
    fun fee(id: String): Result<FeeData>
    fun validate(id: String): Result<RequestResult>
}