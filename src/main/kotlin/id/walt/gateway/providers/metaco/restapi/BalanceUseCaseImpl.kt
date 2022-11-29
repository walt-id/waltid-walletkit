package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.dto.AccountParameter
import id.walt.gateway.dto.BalanceData
import id.walt.gateway.dto.BalanceParameter
import id.walt.gateway.dto.TickerParameter
import id.walt.gateway.providers.metaco.repositories.BalanceRepository
import id.walt.gateway.usecases.BalanceUseCase
import id.walt.gateway.usecases.TickerUseCase

class BalanceUseCaseImpl(
    private val balanceRepository: BalanceRepository,
    private val tickerUseCase: TickerUseCase,
) : BalanceUseCase {
    override fun list(parameter: AccountParameter): Result<List<BalanceData>> = runCatching {
        balanceRepository.findAll(parameter.domainId, parameter.accountId, parameter.criteria).items.map {
            BalanceData(
                amount = it.totalAmount,
                ticker = tickerUseCase.get(TickerParameter(it.tickerId)).getOrThrow()
            )
        }
    }

    override fun get(parameter: BalanceParameter): Result<BalanceData> = runCatching {
        balanceRepository.findAll(parameter.domainId, parameter.accountId, parameter.criteria).items.firstOrNull {
            it.tickerId.equals(parameter.tickerId)
        }?.let{
            BalanceData(
                amount = it.totalAmount,
                ticker = tickerUseCase.get(TickerParameter(it.tickerId)).getOrThrow()
            )
        }?: throw IllegalArgumentException("No balance found for account: ${parameter.accountId} ticker: ${parameter.tickerId} on domain: ${parameter.domainId}")
    }
}