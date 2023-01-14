package id.walt.gateway.usecases

import id.walt.gateway.dto.coins.CoinData
import id.walt.gateway.dto.coins.CoinParameter

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