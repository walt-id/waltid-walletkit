package id.walt.gateway.usecases

import id.walt.gateway.dto.CoinData
import id.walt.gateway.dto.CoinParameter

interface CoinUseCase {
    fun metadata(parameter: CoinParameter): Result<CoinData>
}