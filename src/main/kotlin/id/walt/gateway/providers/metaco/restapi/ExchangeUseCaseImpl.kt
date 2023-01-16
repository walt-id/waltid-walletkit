package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.dto.coins.CoinParameter
import id.walt.gateway.dto.exchanges.ExchangeData
import id.walt.gateway.dto.exchanges.ExchangeParameter
import id.walt.gateway.usecases.CoinUseCase
import id.walt.gateway.usecases.ExchangeUseCase

class ExchangeUseCaseImpl(
    private val coinUseCase: CoinUseCase,
) : ExchangeUseCase {
    override fun exchange(parameter: ExchangeParameter): Result<ExchangeData> = runCatching {
        coinUseCase.metadata(CoinParameter(parameter.from, "eur")).getOrNull()?.price?.let { from ->
            coinUseCase.metadata(CoinParameter(parameter.to, "eur")).getOrNull()?.price?.let { to ->
                parameter.amount.toDoubleOrNull()?.let { amount ->
                    val rate = to / from
                    ExchangeData(
                        amount = (amount * rate).toString(),
                        unitPrice = rate.toString()
                    )
                }
            }
        } ?: throw IllegalArgumentException("Couldn't parse input data")
    }
}