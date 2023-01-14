package id.walt.gateway.usecases

import id.walt.gateway.dto.balances.AccountBalance
import id.walt.gateway.dto.balances.BalanceData
import id.walt.gateway.dto.balances.BalanceParameter
import id.walt.gateway.dto.profiles.ProfileData
import id.walt.gateway.dto.profiles.ProfileParameter
import id.walt.gateway.dto.transactions.TransactionData
import id.walt.gateway.dto.transactions.TransactionListParameter
import id.walt.gateway.dto.transactions.TransactionParameter
import id.walt.gateway.dto.transactions.TransactionTransferData

interface AccountUseCase {

    fun profile(domainId: String, parameter: ProfileParameter): Result<ProfileData>
    fun balance(domainId: String, parameter: ProfileParameter): Result<AccountBalance>
    fun balance(parameter: BalanceParameter): Result<BalanceData>
    fun transactions(parameter: TransactionListParameter): Result<List<TransactionData>>
    fun transaction(parameter: TransactionParameter): Result<TransactionTransferData>
}