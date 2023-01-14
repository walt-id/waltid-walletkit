package id.walt.gateway.usecases

import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.dto.trades.TradeResult
import id.walt.gateway.dto.trades.TradeValidationParameter

interface TradeUseCase {
    fun sell(spend: TradeData, receive: TradeData): Result<TradeResult>
    fun buy(spend: TradeData, receive: TradeData): Result<TradeResult>
    fun send(parameter: TradeData): Result<TradeResult>

    fun validate(parameter: TradeValidationParameter): Result<TradeResult>
}