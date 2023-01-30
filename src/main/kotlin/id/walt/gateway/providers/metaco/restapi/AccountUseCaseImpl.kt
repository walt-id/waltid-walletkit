package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.Common
import id.walt.gateway.dto.AmountWithValue
import id.walt.gateway.dto.TransferData
import id.walt.gateway.dto.ValueWithChange
import id.walt.gateway.dto.accounts.AccountData
import id.walt.gateway.dto.accounts.AccountIdentifier
import id.walt.gateway.dto.accounts.AccountParameter
import id.walt.gateway.dto.balances.AccountBalance
import id.walt.gateway.dto.balances.BalanceData
import id.walt.gateway.dto.balances.BalanceParameter
import id.walt.gateway.dto.profiles.ProfileData
import id.walt.gateway.dto.profiles.ProfileParameter
import id.walt.gateway.dto.tickers.TickerData
import id.walt.gateway.dto.tickers.TickerParameter
import id.walt.gateway.dto.transactions.TransactionData
import id.walt.gateway.dto.transactions.TransactionListParameter
import id.walt.gateway.dto.transactions.TransactionParameter
import id.walt.gateway.dto.transactions.TransactionTransferData
import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.providers.metaco.repositories.*
import id.walt.gateway.providers.metaco.restapi.account.model.Account
import id.walt.gateway.providers.metaco.restapi.transaction.model.Transaction
import id.walt.gateway.providers.metaco.restapi.transfer.model.Transfer
import id.walt.gateway.providers.metaco.restapi.transfer.model.transferparty.AccountTransferParty
import id.walt.gateway.providers.metaco.restapi.transfer.model.transferparty.AddressTransferParty
import id.walt.gateway.providers.metaco.restapi.transfer.model.transferparty.TransferParty
import id.walt.gateway.usecases.AccountUseCase
import id.walt.gateway.usecases.BalanceUseCase
import id.walt.gateway.usecases.TickerUseCase

class AccountUseCaseImpl(
    private val domainRepository: DomainRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val orderRepository: OrderRepository,
    private val transferRepository: TransferRepository,
    private val addressRepository: AddressRepository,
    private val balanceUseCase: BalanceUseCase,
    private val tickerUseCase: TickerUseCase,
) : AccountUseCase {
    override fun profile(parameter: ProfileParameter): Result<ProfileData> = runCatching {
        ProfileData(
            profileId = parameter.id,
            accounts = getProfileAccounts(parameter).map {
                buildProfileData(AccountParameter(AccountIdentifier(it.data.domainId, it.data.id)), it)
            })
    }

    override fun balance(parameter: ProfileParameter): Result<AccountBalance> = runCatching {
        getProfileAccounts(parameter).flatMap {
            balanceUseCase.list(AccountParameter(AccountIdentifier(it.data.domainId, it.data.id))).getOrElse { emptyList() }
        }.filter { !ProviderConfig.tickersIgnore.contains(it.ticker.id) }.let { AccountBalance(it) }
    }

    override fun balance(parameter: BalanceParameter): Result<BalanceData> = runCatching {
        balanceUseCase.get(parameter).getOrThrow()
    }

    override fun transactions(parameter: TransactionListParameter): Result<List<TransactionData>> = runCatching {
        transferRepository.findAll(
            parameter.domainId, mapOf(
                "accountId" to parameter.accountId,
                "sortBy" to "registeredAt",
                "sortOrder" to "DESC",
                parameter.tickerId?.let { "tickerId" to it } ?: Pair("", ""),
            )
        ).filter { !it.transactionId.isNullOrEmpty() }.groupBy { it.transactionId }.map {
            val ticker = getTickerData(parameter.tickerId ?: "")
            buildTransactionData(parameter, it.key!!, it.value, ticker)
        }
    }

    override fun transaction(parameter: TransactionParameter): Result<TransactionTransferData> = runCatching {
        transactionRepository.findById(parameter.domainId, parameter.transactionId).let { transaction ->
            val transfers = transferRepository.findAll(
                parameter.domainId,
                mapOf("transactionId" to transaction.id)
            )
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

    private fun getProfileAccounts(profile: ProfileParameter): List<Account> = let {
        val callback: (domainId: String, id: String) -> List<Account> = getProfileFetchCallback(profile.id)
        domainRepository.findAll(emptyMap()).map {
            runCatching { callback(it.data.id, profile.id) }.getOrDefault(emptyList())
        }.filter {
            it.isNotEmpty()
        }.flatten()
    }

    private fun getProfileFetchCallback(id: String): (domainId: String, accountId: String) -> List<Account> = { domainId, accountId ->
        if (Regex("[a-zA-Z0-9]{8}(-[a-zA-Z0-9]{4}){3}-[a-zA-Z0-9]{12}").matches(id)) {
            listOf(accountRepository.findById(domainId, accountId))
        } else {
            accountRepository.findAll(domainId, mapOf("metadata.customProperties" to "iban:$accountId"))
        }
    }

    private fun getTickerData(tickerId: String) = tickerUseCase.get(TickerParameter(tickerId)).getOrThrow()

    private fun getAccountTickers(account: AccountParameter) = balanceUseCase.list(account).getOrNull() ?: emptyList()

    private fun computeAmount(transfers: List<Transfer>) =
        transfers.filter { it.kind == "Transfer" }.map { it.value.toLongOrNull() ?: 0 }
            .fold(0L) { acc, d -> acc + d }.toString()

    private fun getTransactionStatus(transaction: Transaction) =
        transaction.ledgerTransactionData?.ledgerStatus ?: transaction.processing?.status ?: "Unknown"

    private fun buildTransactionData(parameter: TransactionListParameter, transactionId: String, transfers: List<Transfer>, tickerData: TickerData? = null) =
        let {
            val transaction = transactionRepository.findById(parameter.domainId, transactionId)
            val ticker = tickerData ?: getTickerData(parameter.tickerId ?: transfers.first().tickerId)
            val amount = computeAmount(transfers)
            TransactionData(
                id = transaction.id,
                date = transaction.registeredAt,
                amount = amount,
                ticker = ticker,
                type = getTransactionOrderType(parameter.accountId, transaction),
                status = getTransactionStatus(transaction),
                price = ValueWithChange(
                    Common.computeAmount(amount, ticker.decimals) * ticker.askPrice.value,
                    Common.computeAmount(amount, ticker.decimals) * ticker.askPrice.change
                ),
                relatedAccount = getRelatedAccount(parameter.domainId, transaction.orderReference != null, transfers),
            )
        }

    private fun getRelatedAccount(domainId: String, isSender: Boolean, transfers: List<Transfer>) =
        transfers.filter { it.kind == "Transfer" }.let {
            if (isSender) {
                getTransferAddresses(domainId, it.mapNotNull { it.recipient })
            } else
                getTransferAddresses(domainId, it.flatMap { it.senders })
        }.firstOrNull() ?: "Unknown"

    private fun getTransferAddresses(domainId: String, transferParties: List<TransferParty>) = transferParties.flatMap {
        if (it is AddressTransferParty) listOf(it.address)
        else (it as AccountTransferParty).addressDetails?.let { listOf(it.address) } ?: addressRepository.findAll(
            domainId, it.accountId, emptyMap()
        ).map { it.address }
    }

    private fun buildProfileData(parameter: AccountParameter, account: Account) = AccountData(
        accountIdentifier = AccountIdentifier(account.data.domainId, account.data.id),
        alias = account.data.alias,
        addresses = addressRepository.findAll(account.data.domainId, account.data.id, emptyMap()).map { it.address },
        tickers = getAccountTickers(parameter).map { it.ticker.id }
    )

    private fun getTransactionOrderType(accountId: String, transaction: Transaction) =
        transaction.orderReference?.let {
            runCatching {
                orderRepository.findById(it.domainId, it.id).data
            }.fold(onSuccess = {
                transaction.relatedAccounts.filter { it.id == accountId }.none { it.sender }.takeIf { it }
                    ?.let { "Receive" } ?: it.metadata.customProperties.transactionType ?: "Outgoing"
            }, onFailure = {
                "Unknown"
            })
        } ?: "Receive"
}

