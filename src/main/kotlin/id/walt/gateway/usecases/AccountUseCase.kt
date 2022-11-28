package id.walt.gateway.usecases

import id.walt.gateway.dto.*

interface AccountUseCase {
    fun profile(parameter: AccountParameter): Result<List<ProfileData>>
    fun balance(parameter: AccountParameter): Result<List<BalanceData>>
    fun transactions(parameter: AccountParameter): Result<List<TransactionData>>
    fun transaction(parameter: TransactionParameter): Result<TransactionData>
}