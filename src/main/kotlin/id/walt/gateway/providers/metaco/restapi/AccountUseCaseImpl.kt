package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.dto.*
import id.walt.gateway.providers.metaco.repositories.AccountRepository
import id.walt.gateway.providers.metaco.repositories.BalanceRepository
import id.walt.gateway.usecases.AccountUseCase
import id.walt.gateway.usecases.TickerUseCase

class AccountUseCaseImpl(
    private val accountRepository: AccountRepository,
    private val balanceRepository: BalanceRepository,
    private val tickerUseCase: TickerUseCase,
) : AccountUseCase {
    override fun profile(parameter: AccountParameter): Result<List<ProfileData>> = runCatching {
        accountRepository.findAll(parameter.domainId, parameter.criteria).items.map {
            ProfileData(id = it.data.id, alias = it.data.alias)
        }
    }

    override fun balance(parameter: AccountParameter): Result<List<BalanceData>> = runCatching {
        profile(parameter).fold(onSuccess = {
            it.flatMap {
                balanceRepository.findAll(parameter.domainId, it.id, parameter.criteria).items.map {
                    BalanceData(
                        amount = it.totalAmount,
                        ticker = tickerUseCase.get(TickerParameter(it.tickerId)).getOrThrow()
                    )
                }
            }
        }, onFailure = { throw it })
    }

    override fun transactions(parameter: AccountParameter): Result<List<TransactionData>> {
        TODO("Not yet implemented")
    }

    override fun transaction(parameter: TransactionParameter): Result<TransactionData> {
        TODO("Not yet implemented")
    }
}