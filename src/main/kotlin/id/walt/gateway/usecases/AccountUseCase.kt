package id.walt.gateway.usecases

import id.walt.gateway.dto.*
import id.walt.gateway.dto.trades.TradeListParameter

interface AccountUseCase {

    fun profile(domainId: String, parameter: ProfileParameter): Result<ProfileData>
    fun balance(domainId: String, parameter: ProfileParameter): Result<AccountBalance>
    fun balance(parameter: BalanceParameter): Result<BalanceData>
    fun transactions(parameter: TradeListParameter): Result<List<TransactionData>>
    fun transaction(parameter: TransactionParameter): Result<TransactionTransferData>
}