package id.walt.gateway.usecases

import id.walt.gateway.dto.coins.CoinData
import id.walt.gateway.dto.coins.CoinParameter

interface CoinUseCase {
    fun metadata(parameter: CoinParameter): Result<CoinData>
}