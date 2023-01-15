package id.walt.gateway.providers.coingecko

import id.walt.gateway.dto.coins.CoinData
import id.walt.gateway.dto.coins.CoinParameter
import id.walt.gateway.usecases.CoinUseCase

class SimpleCoinUseCaseImpl(
    private val repository: CoinRepository,
    private val parser: ResponseParser<CoinData>,
) : CoinUseCase {

    override fun metadata(parameter: CoinParameter): Result<CoinData> =
        runCatching { parser.parse(parameter.id, parameter.currency, repository.findById(parameter.id, parameter.currency)) }
}