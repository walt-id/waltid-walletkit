package id.walt.gateway.providers.metaco.restapi

import com.beust.klaxon.Klaxon
import id.walt.gateway.Common
import id.walt.gateway.dto.AmountWithValue
import id.walt.gateway.dto.CreateAccountPayloadData
import id.walt.gateway.dto.TransferData
import id.walt.gateway.dto.accounts.*
import id.walt.gateway.dto.balances.AccountBalance
import id.walt.gateway.dto.balances.BalanceData
import id.walt.gateway.dto.balances.BalanceParameter
import id.walt.gateway.dto.profiles.ProfileData
import id.walt.gateway.dto.profiles.ProfileParameter
import id.walt.gateway.dto.requests.RequestParameter
import id.walt.gateway.dto.requests.RequestResult
import id.walt.gateway.dto.tickers.TickerParameter
import id.walt.gateway.dto.transactions.TransactionData
import id.walt.gateway.dto.transactions.TransactionListParameter
import id.walt.gateway.dto.transactions.TransactionParameter
import id.walt.gateway.dto.transactions.TransactionTransferData
import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.providers.metaco.repositories.*
import id.walt.gateway.providers.metaco.restapi.account.model.Account
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.Payload
import id.walt.gateway.providers.metaco.restapi.models.customproperties.TransactionCustomProperties
import id.walt.gateway.providers.metaco.restapi.models.customproperties.toMap
import id.walt.gateway.providers.metaco.restapi.order.model.Order
import id.walt.gateway.providers.metaco.restapi.transaction.model.RelatedAccount
import id.walt.gateway.providers.metaco.restapi.transaction.model.Transaction
import id.walt.gateway.providers.metaco.restapi.transfer.model.Transfer
import id.walt.gateway.providers.metaco.restapi.transfer.model.transferparty.AccountTransferParty
import id.walt.gateway.providers.metaco.restapi.transfer.model.transferparty.AddressTransferParty
import id.walt.gateway.providers.metaco.restapi.transfer.model.transferparty.TransferParty
import id.walt.gateway.usecases.AccountUseCase
import id.walt.gateway.usecases.BalanceUseCase
import id.walt.gateway.usecases.RequestUseCase
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
    private val requestUseCase: RequestUseCase,
) : AccountUseCase {
    override fun profile(parameter: ProfileParameter): Result<ProfileData> = runCatching {
        ProfileData(
            profileId = parameter.id,
            accounts = getProfileAccounts(parameter).map {
                buildProfileData(AccountParameter(AccountIdentifier(it.data.domainId, it.data.id)), it)
            })
    }

    override fun create(parameter: AccountInitiationParameter): Result<RequestResult> = runCatching {
        val domain = domainRepository.findAll(emptyMap()).first { it.data.alias == parameter.domainName }
        if (!checkAccountExists(domain.data.id, parameter.accountName)) {
            requestUseCase.create(
                RequestParameter(
                    payloadType = Payload.Types.CreateAccount.value,
                    targetDomainId = domain.data.id,
                    data = CreateAccountPayloadData(
                        alias = parameter.accountName,
                        ledgerId = parameter.ledgerId,
                        lock = "Unlocked",
                        keyStrategy = "VaultHard",
                        vaultId = "00000000-0000-0000-0000-000000000000"
                    ),
                )
            )
            "${parameter.domainName} ${parameter.accountName}"
        } else {
            throw Exception("account already exists")
        }
    }.fold(
        onSuccess = {
            Result.success(RequestResult(true, it))
        }, onFailure = {
            Result.failure(Exception("Exception(\"Couldn't create account ${parameter.accountName} in domain ${parameter.domainName}\"): ${it.message}"))
        }
    )

    override fun list(): Result<List<AccountBasicData>> = runCatching {
        domainRepository.findAll(emptyMap()).map { domain ->
            accountRepository.findAll(domain.data.id, emptyMap()).map { account ->
                AccountBasicData(
                    domainName = domain.data.alias,
                    accountAlias = account.data.alias ?: "no-name",
                    address = addressRepository.findAll(domain.data.id, account.data.id, emptyMap()).map { it.address },
                )
            }
        }.flatten()
    }

    override fun balance(parameter: ProfileParameter): Result<AccountBalance> = runCatching {
        getProfileAccounts(parameter).flatMap {
            balanceUseCase.list(AccountParameter(AccountIdentifier(it.data.domainId, it.data.id))).getOrElse { emptyList() }
                .sortedBy {
                    it.ticker.name
                }
        }.filter { !ProviderConfig.tickersIgnore.contains(it.ticker.id) }.let { AccountBalance(it) }
    }

    override fun balance(parameter: BalanceParameter): Result<BalanceData> = runCatching {
        balanceUseCase.get(parameter).getOrThrow()
    }

    override fun transactions(parameter: TransactionListParameter): Result<List<TransactionData>> = runCatching {
        val transactions =
            transactionRepository.findAll(parameter.domainId, mapOf("accountId" to parameter.accountId)).groupBy { it.id }
        val orders =
            orderRepository.findAll(parameter.domainId, mapOf("accountId" to parameter.accountId)).groupBy { it.data.id }
        transferRepository.findAll(
            parameter.domainId, mapOf(
                "accountId" to parameter.accountId,
                "sortBy" to "registeredAt",
                "sortOrder" to "DESC",
                parameter.tickerId?.let { "tickerId" to it } ?: Pair("", ""),
            )
        ).filter { !it.transactionId.isNullOrEmpty() }.groupBy { it.transactionId!! }.map {
            val transaction = transactions[it.key]?.first()
            val order = orders[transaction?.orderReference?.id]?.first()
            buildTransactionData(
                parameter, parameter.tickerId ?: it.value.first().tickerId, it.value, transaction, order
            )
        }
    }

    override fun transaction(parameter: TransactionParameter): Result<TransactionTransferData> = runCatching {
        transactionRepository.findById(parameter.domainId, parameter.transactionId).let { transaction ->
            val transfers = transferRepository.findAll(
                parameter.domainId,
                mapOf("transactionId" to parameter.transactionId, "accountId" to parameter.accountId)
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
                        address = getRelatedAccount(parameter.accountId, transfers),
                    )
                }
            )
        }
    }

    private fun checkAccountExists(domainId: String, alias: String) =
        accountRepository.findAll(domainId, emptyMap()).any {
            it.data.alias == alias
        }

    private fun getProfileAccounts(profile: ProfileParameter): List<Account> = let {
        val callback: (domainId: String, id: String) -> List<Account> = getProfileFetchCallback(profile.id)
        domainRepository.findAll(emptyMap()).map {
            runCatching { callback(it.data.id, profile.id) }.getOrDefault(emptyList())
        }.filter {
            it.isNotEmpty()
        }.flatten()
    }

    private fun getProfileFetchCallback(id: String): (domainId: String, accountId: String) -> List<Account> =
        { domainId, accountId ->
            if (Regex("[a-zA-Z0-9]{8}(-[a-zA-Z0-9]{4}){3}-[a-zA-Z0-9]{12}").matches(id)) {
                listOf(accountRepository.findById(domainId, accountId))
            } else if (Regex("[a-zA-Z]{3,7}[0-9]{2}").matches(id)) {
                accountRepository.findAll(domainId, emptyMap()).filter {
                    it.data.alias.equals(id)
                }
            } else {
                accountRepository.findAll(domainId, mapOf("metadata.customProperties" to "iban:$accountId"))
            }
        }

    private fun getTickerData(tickerId: String) = tickerUseCase.get(TickerParameter(tickerId)).getOrThrow()

    private fun getAccountTickers(account: AccountParameter) = balanceUseCase.list(account).getOrNull() ?: emptyList()

    private fun computeAmount(transfers: List<Transfer>) =
        transfers.filter { it.kind == "Transfer" }.map { it.value.toLongOrNull() ?: 0 }
            .fold(0L) { acc, d -> acc + d }.toString()

    private fun getTransactionStatus(transaction: Transaction?) =
        transaction?.ledgerTransactionData?.ledgerStatus ?: transaction?.processing?.status ?: "Unknown"

    private fun buildTransactionData(
        parameter: TransactionListParameter,
        tickerId: String,
        transfers: List<Transfer>,
        transaction: Transaction?,
        order: Order?,
    ) = let {
        val amount = computeAmount(transfers)
        TransactionData(
            id = transaction?.id ?: "",
            date = transaction?.registeredAt ?: transfers.first().registeredAt,
            amount = amount,
            status = getTransactionStatus(transaction),
            meta = let {
                getTransactionMeta(order) ?: Common.getTransactionMeta(
                    tradeType = getTransactionOrderType(parameter.accountId, transaction?.relatedAccounts ?: emptyList()),
                    amount = amount,
                    ticker = getTickerData(tickerId),
                )
            }.toMap(),
            relatedAccount = getRelatedAccount(parameter.accountId, transfers),
        )
    }

    private fun getTransactionMeta(order: Order? = null) = order?.let {
        it.data.metadata.customProperties["transactionProperties"]?.let {
            runCatching { Klaxon().parse<TransactionCustomProperties>(it) }.getOrNull()
        }
    }

    private fun getRelatedAccount(accountId: String, transfers: List<Transfer>) = let {
        val filtered = transfers.filter { it.kind == "Transfer" }
        filtered.flatMap { it.senders }.takeIf {
            it.any { (it as? AccountTransferParty)?.accountId == accountId }
        }?.let {
            filtered.mapNotNull { it.recipient }
        } ?: let {
            filtered.flatMap { it.senders }
        }
    }.let {
        getTransferAddresses(it)
    }.firstOrNull() ?: "Unknown"

    private fun getTransferAddresses(transferParties: List<TransferParty>) = transferParties.flatMap {
        if (it is AddressTransferParty) listOf(it.address)
        else (it as AccountTransferParty).addressDetails?.let { listOf(it.address) } ?: addressRepository.findAll(
            it.domainId, it.accountId, emptyMap()
        ).map { it.address }
    }

    private fun buildProfileData(parameter: AccountParameter, account: Account) = AccountData(
        accountIdentifier = AccountIdentifier(account.data.domainId, account.data.id),
        alias = account.data.alias,
        addresses = addressRepository.findAll(account.data.domainId, account.data.id, emptyMap()).map { it.address },
        tickers = getAccountTickers(parameter).map { it.ticker.id }
    )

    private fun getTransactionOrderType(accountId: String, relatedAccounts: List<RelatedAccount>) =
        relatedAccounts.filter { it.id == accountId }.none { it.sender }.takeIf { it }?.let { "Receive" } ?: "Outgoing"
}

