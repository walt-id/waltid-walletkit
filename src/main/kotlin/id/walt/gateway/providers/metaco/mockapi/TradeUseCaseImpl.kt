package id.walt.gateway.providers.metaco.mockapi

import id.walt.gateway.Common
import id.walt.gateway.dto.trades.BuyParameter
import id.walt.gateway.dto.trades.*
import id.walt.gateway.usecases.TradeUseCase

class TradeUseCaseImpl: TradeUseCase {
    override fun sell(parameter: SellParameter): Result<SellData> = Result.success(getSellTrade())

    override fun buy(parameter: BuyParameter): Result<BuyData> = Result.success(getBuyTrade())

    override fun send(parameter: SendParameter): Result<SendData> = Result.success(getSendTrade())

    override fun validate(parameter: TradeValidationParameter): Result<TradeValidationData> =
        Result.success(getTradeValidation())

    private fun getSellTrade() = SellData(
        status = "Order created."
    )

    private fun getBuyTrade() = BuyData(
        status = "Order created."
    )

    private fun getSendTrade() = SendData(
        status = "Order created."
    )

    private fun getTradeValidation() = (Common.getRandomInt(from = 0, to = 2) % 2 == 0).let {
        TradeValidationData(
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