package id.walt.gateway.usecases

import id.walt.gateway.dto.AccountParameter
import id.walt.gateway.dto.BalanceData
import id.walt.gateway.dto.BalanceParameter

interface BalanceUseCase {

    fun list(parameter: AccountParameter): Result<List<BalanceData>>
    fun get(parameter: BalanceParameter): Result<BalanceData>
}