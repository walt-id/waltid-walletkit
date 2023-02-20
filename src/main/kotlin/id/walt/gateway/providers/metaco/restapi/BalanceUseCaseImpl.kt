package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.dto.accounts.AccountParameter
import id.walt.gateway.dto.balances.BalanceData
import id.walt.gateway.dto.balances.BalanceParameter
import id.walt.gateway.dto.tickers.TickerParameter
import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.providers.metaco.repositories.BalanceRepository
import id.walt.gateway.providers.metaco.restapi.balance.model.Balance
import id.walt.gateway.usecases.BalanceUseCase
import id.walt.gateway.usecases.TickerUseCase

class BalanceUseCaseImpl(
    private val balanceRepository: BalanceRepository,
    private val tickerUseCase: TickerUseCase,
) : BalanceUseCase {
    override fun list(parameter: AccountParameter): Result<List<BalanceData>> = runCatching {
        balanceRepository.findAll(
            parameter.accountIdentifier.domainId,
            parameter.accountIdentifier.accountId,
            parameter.criteria
        ).mapNotNull {
            buildBalanceData(it)
        }
    }

    override fun get(parameter: BalanceParameter): Result<BalanceData> = runCatching {
        balanceRepository.findAll(parameter.domainId, parameter.accountId, parameter.criteria).firstOrNull {
            it.tickerId.equals(parameter.tickerId)
        }?.let {
            buildBalanceData(it)
        }
            ?: throw IllegalArgumentException("No balance found for account: ${parameter.accountId} ticker: ${parameter.tickerId} on domain: ${parameter.domainId}")
    }

    private fun buildBalanceData(balance: Balance): BalanceData? = balance.takeIf {
        ProviderConfig.tickersWhitelist.contains(it.tickerId)
    }?.let {
        BalanceData(
            amount = balance.totalAmount,
            ticker = tickerUseCase.get(TickerParameter(balance.tickerId)).getOrThrow()
        )
    }
}
