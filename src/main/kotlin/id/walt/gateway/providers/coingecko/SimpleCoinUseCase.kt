package id.walt.gateway.providers.coingecko

import id.walt.gateway.dto.CoinData
import id.walt.gateway.dto.CoinParameter
import id.walt.gateway.usecases.CoinUseCase

class SimpleCoinUseCase(
    private val repository: CoinRepository,
    private val parser: ResponseParser<CoinData>,
) : CoinUseCase {

    override fun metadata(parameter: CoinParameter): Result<CoinData> =
        runCatching { parser.parse(repository.findById(parameter.id, parameter.currency)) }
}