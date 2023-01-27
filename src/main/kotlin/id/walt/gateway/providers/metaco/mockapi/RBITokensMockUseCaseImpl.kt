package id.walt.gateway.providers.metaco.mockapi

import id.walt.gateway.dto.coins.CoinData
import id.walt.gateway.dto.coins.CoinParameter
import id.walt.gateway.usecases.CoinUseCase

class RBITokensMockUseCaseImpl : CoinUseCase {
    override fun metadata(parameter: CoinParameter): Result<CoinData> = when (parameter.id) {
        // Jan.09.'23
        "euro" -> CoinData(
            askPrice = 1.0, marketCap = -1.0, change = .0
        )

        "gold" -> CoinData(
            askPrice = 56.62, marketCap = 11520 * Math.pow(10.0, 12.0), change = 0.05
        )

        "polygon testnet mumbai" -> CoinData(
            askPrice = 0.807364, marketCap = 7262393283.086078, change = 0.047595272
        )

        else -> null
    }?.let { Result.success(it) } ?: Result.failure(Exception("No coin data found"))
}