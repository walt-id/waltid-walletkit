package id.walt.gateway.usecases

import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.dto.trades.OrderResult
import id.walt.gateway.dto.trades.TradeValidationParameter

interface TradeUseCase {
    fun sell(spend: TradeData, receive: TradeData): Result<OrderResult>
    fun buy(spend: TradeData, receive: TradeData): Result<OrderResult>
    fun send(parameter: TradeData): Result<OrderResult>

    fun validate(parameter: TradeValidationParameter): Result<OrderResult>
}