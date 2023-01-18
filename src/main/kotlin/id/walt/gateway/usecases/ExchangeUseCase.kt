package id.walt.gateway.usecases

import id.walt.gateway.dto.exchanges.ExchangeData
import id.walt.gateway.dto.exchanges.ExchangeParameter

interface ExchangeUseCase {
    fun exchange(parameter: ExchangeParameter): Result<ExchangeData>
}