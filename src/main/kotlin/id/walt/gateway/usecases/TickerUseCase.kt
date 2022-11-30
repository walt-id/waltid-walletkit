package id.walt.gateway.usecases

import id.walt.gateway.dto.TickerData
import id.walt.gateway.dto.TickerParameter

interface TickerUseCase {
    fun get(parameter: TickerParameter): Result<TickerData>
}