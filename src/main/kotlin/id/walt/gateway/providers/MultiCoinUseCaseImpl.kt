package id.walt.gateway.providers

import id.walt.gateway.dto.CoinData
import id.walt.gateway.dto.CoinParameter
import id.walt.gateway.usecases.CoinUseCase

class MultiCoinUseCaseImpl(
    private vararg val coinUseCases: CoinUseCase
) : CoinUseCase {
    override fun metadata(parameter: CoinParameter): Result<CoinData> = let {
        var i = 0
        var result: Result<CoinData>
        do {
            result = coinUseCases[i++].metadata(parameter)
        } while (result.isFailure && i < coinUseCases.size)
        return result
    }
}