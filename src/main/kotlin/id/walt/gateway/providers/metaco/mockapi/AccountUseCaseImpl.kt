package id.walt.gateway.providers.metaco.mockapi

import id.walt.gateway.Common
import id.walt.gateway.dto.*
import id.walt.gateway.usecases.AccountUseCase
import java.util.*

class AccountUseCaseImpl : AccountUseCase {

    override fun profile(parameter: AccountParameter) = Result.success(getProfile(parameter.accountId))

    override fun balance(parameter: AccountParameter) = Result.success((1..7).map { getBalance() })

    override fun transactions(parameter: AccountParameter) =
        Result.success((1..24).map { getTransaction(UUID.randomUUID().toString()) })

    override fun transaction(parameter: TransactionParameter) = Result.success(getTransaction(parameter.transactionId))

    private fun getProfile(id: String) = ProfileData(
        id = id,
        alias = Common.getRandomString(7),
    )

    private fun getBalance() = BalanceData(
        amount = Common.getRandomString(3, true),
        price = getPrice(),
        ticker = getTickerData(),
    )

    private fun getTickerData() = TickerData(
        name = Common.getRandomString(4),
        price = ValueWithChange(Common.getRandomString(2, true), Common.getRandomString(1, true)),
        imageUrl = Common.getRandomString(15),
    )

    private fun getPrice() = ValueWithChange(Common.getRandomString(4, true), Common.getRandomString(1, true))

    private fun getTransaction(id: String) = TransactionData(
        id = id,
        sender = Common.getRandomString(12),
        recipient = Common.getRandomString(12),
        amount = Common.getRandomString(3, true),
        ticker = getTickerData(),
        price = getPrice(),
    )
}