package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.Common
import id.walt.gateway.dto.*
import id.walt.gateway.dto.trades.TradeListParameter
import id.walt.gateway.providers.metaco.repositories.AccountRepository
import id.walt.gateway.providers.metaco.repositories.AddressRepository
import id.walt.gateway.providers.metaco.repositories.TransactionRepository
import id.walt.gateway.providers.metaco.repositories.TransferRepository
import id.walt.gateway.providers.metaco.restapi.account.model.Account
import id.walt.gateway.providers.metaco.restapi.transaction.model.Transaction
import id.walt.gateway.providers.metaco.restapi.transfer.model.transferparty.AccountTransferParty
import id.walt.gateway.providers.metaco.restapi.transfer.model.transferparty.AddressTransferParty
import id.walt.gateway.providers.metaco.restapi.transfer.model.Transfer
import id.walt.gateway.providers.metaco.restapi.transfer.model.transferparty.TransferParty
import id.walt.gateway.usecases.AccountUseCase
import id.walt.gateway.usecases.BalanceUseCase
import id.walt.gateway.usecases.TickerUseCase

class AccountUseCaseImpl(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val transferRepository: TransferRepository,
    private val addressRepository: AddressRepository,
    private val balanceUseCase: BalanceUseCase,
    private val tickerUseCase: TickerUseCase,
) : AccountUseCase {
    override fun profile(domainId: String, parameter: ProfileParameter): Result<ProfileData> = runCatching {
        ProfileData(
            profileId = parameter.id,
            accounts = getProfileAccounts(domainId, parameter).map {
                buildProfileData(AccountParameter(domainId, parameter.id), it)
            })
    }

    override fun balance(domainId: String, parameter: ProfileParameter): Result<AccountBalance> = runCatching {
        getProfileAccounts(domainId, parameter).flatMap {
            balanceUseCase.list(AccountParameter(it.data.domainId, it.data.id)).getOrElse { emptyList() }
        }.let { AccountBalance(it) }
    }

    override fun balance(parameter: BalanceParameter): Result<BalanceData> = runCatching {
        balanceUseCase.get(parameter).getOrThrow()
    }

    override fun transactions(parameter: TradeListParameter): Result<List<TransactionData>> = runCatching {
        transferRepository.findAll(
            parameter.domainId, mapOf(
                "accountId" to parameter.accountId,
                "sortBy" to "registeredAt",
                "sortOrder" to "DESC",
                parameter.tickerId?.let { "tickerId" to it } ?: Pair("", ""),
            )
        ).items.filter { !it.transactionId.isNullOrEmpty() }.groupBy { it.transactionId }.map {
            val ticker = getTickerData(parameter.tickerId ?: "")
            buildTransactionData(parameter, it.key!!, it.value, ticker)
        }//.sortedByDescending { Instant.parse(it.date) }
    }

    override fun transaction(parameter: TransactionParameter): Result<TransactionTransferData> = runCatching {
        transactionRepository.findById(parameter.domainId, parameter.transactionId).let { transaction ->
            val transfers = transferRepository.findAll(
                parameter.domainId,
                mapOf("transactionId" to transaction.id)
            ).items
            val ticker = getTickerData(transfers.first().tickerId)
            val amount = computeAmount(transfers)
            TransactionTransferData(
                status = getTransactionStatus(transaction),
                date = transaction.registeredAt,
                total = AmountWithValue(amount, ticker),
                transfers = transfers.map {
                    TransferData(
                        amount = it.value,
                        type = it.kind,
                        address = getRelatedAccount(parameter.domainId, transaction.orderReference != null, transfers),
                    )
                }
            )
        }
    }

    private fun getProfileAccounts(domainId: String, profile: ProfileParameter) = let {
        if (Regex("[a-zA-Z0-9]{8}(-[a-zA-Z0-9]{4}){3}-[a-zA-Z0-9]{12}").matches(profile.id)) {
            listOf(accountRepository.findById(domainId, profile.id))
        } else {
            accountRepository.findAll(domainId, mapOf("metadata.customProperties" to "iban:${profile.id}")).items
        }
    }

    private fun getTickerData(tickerId: String) = tickerUseCase.get(TickerParameter(tickerId)).getOrThrow()

    private fun getAccountTickers(account: AccountParameter) = balanceUseCase.list(account).getOrNull() ?: emptyList()

    private fun computeAmount(transfers: List<Transfer>) =
        transfers.filter { it.kind == "Transfer" }.map { it.value.toIntOrNull() ?: 0 }
            .fold(0) { acc, d -> acc + d }.toString()

    private fun getTransactionStatus(transaction: Transaction) =
        transaction.ledgerTransactionData?.ledgerStatus ?: transaction.processing?.status ?: "Unknown"

    private fun buildTransactionData(parameter: TradeListParameter, transactionId: String, transfers: List<Transfer>, tickerData: TickerData? = null) =
        let {
            val transaction = transactionRepository.findById(parameter.domainId, transactionId)
            val ticker = tickerData ?: getTickerData(parameter.tickerId ?: transfers.first().tickerId)
            val amount = computeAmount(transfers)
            TransactionData(
                id = transaction.id,
                date = transaction.registeredAt,
                amount = amount,
                ticker = ticker,
                //TODO: get outgoing status from order custom properties
                type = transaction.orderReference?.let { "Outgoing" } ?: "Receive",
                status = getTransactionStatus(transaction),
                price = ValueWithChange(
                    Common.computeAmount(amount, ticker.decimals) * ticker.price.value,
                    Common.computeAmount(amount, ticker.decimals) * ticker.price.change
                ),
                relatedAccount = getRelatedAccount(parameter.domainId, transaction.orderReference != null, transfers),
            )
        }

    private fun getRelatedAccount(domainId: String, isSender: Boolean, transfers: List<Transfer>) =
        transfers.filter { it.kind == "Transfer" }.let {
            if (isSender) {
                getAddresses(domainId, it.mapNotNull { it.recipient })
            } else
                getAddresses(domainId, it.flatMap { it.senders })
        }.firstOrNull() ?: "Unknown"

    private fun getAddresses(domainId: String, transferParties: List<TransferParty>) = transferParties.flatMap {
        if (it is AddressTransferParty) listOf(it.address)
        else (it as AccountTransferParty).addressDetails?.let { listOf(it.address) } ?: addressRepository.findAll(
            domainId,
            it.accountId
        ).items.map { it.address }
    }

    private fun buildProfileData(parameter: AccountParameter, account: Account) = AccountData(
        accountId = account.data.id,
        alias = account.data.alias,
        addresses = emptyList(),
        tickers = getAccountTickers(parameter).map { it.ticker.id }
    )
}

