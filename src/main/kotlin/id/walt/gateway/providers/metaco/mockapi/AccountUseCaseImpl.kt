package id.walt.gateway.providers.metaco.mockapi

import id.walt.gateway.Common
import id.walt.gateway.dto.*
import id.walt.gateway.usecases.AccountUseCase
import java.util.*

class AccountUseCaseImpl : AccountUseCase {

    override fun profile(parameter: AccountParameter) = Result.success(listOf(getProfile(parameter.accountId)))

    override fun balance(parameter: AccountParameter) = Result.success((1..7).map { getBalance() })

    override fun transactions(parameter: AccountParameter) =
        Result.success((1..24).map { getTransaction(UUID.randomUUID().toString()) })

    override fun transaction(parameter: TransactionParameter) = Result.success(getTransaction(parameter.transactionId))

    private fun getProfile(id: String) = ProfileData(
        id = id,
        alias = Common.getRandomString(7),
    )

    private fun getBalance() = BalanceData(
        amount = Common.getRandomLong(1000, 1000000).toString(),
        ticker = getTickerData(),
    )

    private fun getTickerData() = TickerData(
        name = Common.getRandomString(4),
        price = getPrice(),
        imageUrl = Common.getRandomString(15),
        decimals = Common.getRandomLong(1000, 10000),
    )

    private fun getPrice() = ValueWithChange(Common.getRandomDouble(.1, 1000.0), Common.getRandomDouble(.1, 1000.0))

    private fun getTransaction(id: String) = TransactionData(
        id = id,
        sender = Common.getRandomString(12),
        recipient = Common.getRandomString(12),
        amount = Common.getRandomString(3, true),
        ticker = getTickerData(),
        price = getPrice(),
    )
}