package id.walt.gateway.providers.metaco.mockapi

import id.walt.gateway.Common
import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.dto.trades.TradeResult
import id.walt.gateway.dto.trades.TradeValidationParameter
import id.walt.gateway.usecases.TradeUseCase
import java.util.*

class TradeUseCaseImpl : TradeUseCase {
    override fun sell(parameter: TradeData): Result<TradeResult> = Result.success(getTradeData())

    override fun buy(parameter: TradeData): Result<TradeResult> = Result.success(getTradeData())

    override fun send(parameter: TradeData): Result<TradeResult> = Result.success(getTradeData())

    override fun validate(parameter: TradeValidationParameter): Result<TradeResult> =
        Result.success(getTradeValidation())

    private fun getTradeData() = (Common.getRandomInt(from = 0, to = 2) % 2 == 0).let {
        TradeResult(
            result = it,
            message = if (!it) "Error occurred." else UUID.randomUUID().toString()
        )
    }

    private fun getTradeValidation() = (Common.getRandomInt(from = 0, to = 2) % 2 == 0).let {
        TradeResult(
            result = it,
            message = if (!it) messages[Common.getRandomInt(0, messages.size)] else "Validation passed successfully."
        )
    }

    private val messages = listOf(
        "Order type not matching account's ledger.",
        "Currently proposed fee exceeds maximum fee. Estimate 257 eth.",
        "Not enough funds.",
        "Currently proposed fee exceeds maximum fee.",
    )
}