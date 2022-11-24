package id.walt.gateway.usecases

import id.walt.gateway.dto.*

interface TransactionUseCase {
    fun sell(parameter: SellParameter): Result<SellData>
    fun buy(parameter: BuyParameter): Result<BuyData>
    fun transfer(parameter: TransferParameter): Result<TransferData>
}