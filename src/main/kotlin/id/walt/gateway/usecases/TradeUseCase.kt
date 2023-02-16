package id.walt.gateway.usecases

import id.walt.gateway.dto.requests.RequestResult
import id.walt.gateway.dto.trades.TradeData

interface TradeUseCase {
    fun sell(spend: TradeData, receive: TradeData): Result<RequestResult>
    fun buy(spend: TradeData, receive: TradeData): Result<RequestResult>
    fun send(send: TradeData): Result<RequestResult>
    fun validate(parameter: TradeData): Result<RequestResult>
}