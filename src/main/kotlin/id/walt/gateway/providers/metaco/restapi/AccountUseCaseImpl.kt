package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.dto.*
import id.walt.gateway.providers.metaco.repositories.AccountRepository
import id.walt.gateway.providers.metaco.repositories.BalanceRepository
import id.walt.gateway.providers.metaco.repositories.TransactionRepository
import id.walt.gateway.providers.metaco.repositories.TransferRepository
import id.walt.gateway.usecases.AccountUseCase
import id.walt.gateway.usecases.CoinUseCase

class AccountUseCaseImpl(
    private val accountRepository: AccountRepository,
    private val balanceRepository: BalanceRepository,
    private val transactionRepository: TransactionRepository,
    private val transferRepository: TransferRepository,
    private val coinUseCase: CoinUseCase,
) : AccountUseCase {
    override fun profile(parameter: AccountParameter): Result<ProfileData> {
        TODO("Not yet implemented")
    }

    override fun balance(parameter: AccountParameter): Result<List<BalanceData>> {
        TODO("Not yet implemented")
    }

    override fun transactions(parameter: AccountParameter): Result<List<TransactionData>> {
        TODO("Not yet implemented")
    }

    override fun transaction(parameter: TransactionParameter): Result<TransactionData> {
        TODO("Not yet implemented")
    }
}