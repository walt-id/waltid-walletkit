package id.walt.gateway.providers.rcb

import id.walt.gateway.dto.CoinData
import id.walt.gateway.dto.CoinParameter
import id.walt.gateway.usecases.CoinUseCase

class CoinUseCaseImpl(
    private val repository: CoinRepositoryImpl,
    private val parser: ResponseParser<CoinData>,
) : CoinUseCase {
    override fun metadata(parameter: CoinParameter): Result<CoinData> = runCatching {
        parser.parse(repository.findById(parameter.id))
    }
}