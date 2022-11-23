package id.walt.gateway.providers.metaco.mockapi

import id.walt.gateway.dto.*
import id.walt.gateway.usecases.TransactionUseCase

class TransactionUseCaseImpl: TransactionUseCase {
    override fun sell(parameter: SellParameter): Result<SellData> {
        TODO("Not yet implemented")
    }

    override fun buy(parameter: BuyParameter): Result<BuyData> {
        TODO("Not yet implemented")
    }

    override fun transfer(parameter: TransferParameter): Result<TransferData> {
        TODO("Not yet implemented")
    }
}