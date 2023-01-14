package id.walt.gateway.usecases

import id.walt.gateway.dto.accounts.AccountParameter
import id.walt.gateway.dto.balances.BalanceData
import id.walt.gateway.dto.balances.BalanceParameter

interface BalanceUseCase {

    fun list(parameter: AccountParameter): Result<List<BalanceData>>
    fun get(parameter: BalanceParameter): Result<BalanceData>
}