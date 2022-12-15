package id.walt.gateway.usecases

import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.dto.trades.TradeValidationParameter

interface TradeUseCase {
    fun sell(parameter: TradeValidationParameter): Result<TradeData>
    fun buy(parameter: TradeValidationParameter): Result<TradeData>
    fun send(parameter: TradeValidationParameter): Result<TradeData>

    fun validate(parameter: TradeValidationParameter): Result<TradeData>
}