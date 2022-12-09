package id.walt.gateway.usecases

import id.walt.gateway.dto.trades.BuyParameter
import id.walt.gateway.dto.trades.*

interface TradeUseCase {
    fun sell(parameter: SellParameter): Result<SellData>
    fun buy(parameter: BuyParameter): Result<BuyData>
    fun send(parameter: SendParameter): Result<SendData>

    fun validate(parameter: TradeValidationParameter): Result<TradeValidationData>
}