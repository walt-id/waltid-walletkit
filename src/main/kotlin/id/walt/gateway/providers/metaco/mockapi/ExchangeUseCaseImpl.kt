package id.walt.gateway.providers.metaco.mockapi

import id.walt.gateway.Common
import id.walt.gateway.dto.exchanges.ExchangeData
import id.walt.gateway.dto.exchanges.ExchangeParameter
import id.walt.gateway.usecases.ExchangeUseCase

class ExchangeUseCaseImpl : ExchangeUseCase {
    override fun exchange(parameter: ExchangeParameter): Result<ExchangeData> = Result.success(
        ExchangeData(
            amount = Common.getRandomDouble(10.0, 100.0).toString(),
            unitPrice = Common.getRandomDouble(1.0, 100.0).toString()
        )
    )
}