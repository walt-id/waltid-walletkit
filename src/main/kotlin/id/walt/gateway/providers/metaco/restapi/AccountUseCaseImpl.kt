package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.dto.*
import id.walt.gateway.providers.metaco.repositories.AccountRepository
import id.walt.gateway.usecases.AccountUseCase
import id.walt.gateway.usecases.BalanceUseCase

class AccountUseCaseImpl(
    private val accountRepository: AccountRepository,
    private val balanceUseCase: BalanceUseCase,
//    private val transactionRepository: TransactionRepository,
) : AccountUseCase {
    override fun profile(parameter: AccountParameter): Result<List<ProfileData>> = runCatching {
        accountRepository.findAll(parameter.domainId, parameter.criteria).items.map {
            ProfileData(id = it.data.id, alias = it.data.alias, addresses = emptyList(), ticker = "TODO")
        }
    }

    override fun balance(parameter: AccountParameter): Result<AccountBalance> = runCatching {
        profile(parameter).fold(onSuccess = {
            balanceUseCase.list(parameter).getOrElse { emptyList() }
        }, onFailure = { throw it }).let {
            AccountBalance(it)
        }
    }

    override fun balance(parameter: BalanceParameter): Result<BalanceData> = runCatching {
        balanceUseCase.get(parameter).getOrThrow()
    }

    override fun transactions(parameter: AccountParameter): Result<List<TransactionData>> {
//        runCatching {
//        profile(parameter).fold(onSuccess = {
//            it.flatMap {
//                transactionRepository.findAll(parameter.domainId, mapOf("accountId" to it.id)).items.map {
//                    TransactionData(
//                        id = it.id,
//                        amount = it.
//                    )
//                }
//            }
//        }, onFailure = {
//            throw it
//        })
        TODO("Not yet implemented")
    }

    override fun transaction(parameter: TransactionParameter): Result<TransactionTransferData> {
        TODO("Not yet implemented")
    }
}