package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.Common
import id.walt.gateway.dto.*
import id.walt.gateway.providers.metaco.repositories.*
import id.walt.gateway.providers.metaco.restapi.address.model.Address
import id.walt.gateway.providers.metaco.restapi.transaction.model.Transaction
import id.walt.gateway.providers.metaco.restapi.transfer.model.AccountTransferParty
import id.walt.gateway.providers.metaco.restapi.transfer.model.AddressTransferParty
import id.walt.gateway.providers.metaco.restapi.transfer.model.Transfer
import id.walt.gateway.providers.metaco.restapi.transfer.model.TransferParty
import id.walt.gateway.usecases.AccountUseCase
import id.walt.gateway.usecases.BalanceUseCase
import id.walt.gateway.usecases.TickerUseCase

class AccountUseCaseImpl(
    private val accountRepository: AccountRepository,
    private val balanceUseCase: BalanceUseCase,
    private val transactionRepository: TransactionRepository,
    private val transferRepository: TransferRepository,
    private val addressRepository: AddressRepository,
    private val tickerUseCase: TickerUseCase,
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

    override fun transactions(parameter: AccountParameter): Result<List<TransactionData>> = runCatching {
        profile(parameter).fold(onSuccess = {
            it.flatMap {
                transactionRepository.findAll(parameter.domainId, mapOf("accountId" to it.id)).items.map {
                    val transfers = transferRepository.findAll(
                        parameter.domainId,
                        mapOf("transactionId" to it.id)
                    ).items.filter { it.kind == "Transfer" }
                    val ticker = tickerUseCase.get(TickerParameter(transfers.first().tickerId)).getOrThrow()
                    val myAddresses = addressRepository.findAll(parameter.domainId, parameter.accountId).items.map { it.address }
                    buildTransactionData(parameter.domainId, it, transfers, myAddresses, ticker)
                }
            }
        }, onFailure = {
            throw it
        })
    }

    override fun transaction(parameter: TransactionParameter): Result<TransactionTransferData> {
        TODO("Not yet implemented")
    }

    private fun buildTransactionData(
        domainId: String,
        transaction: Transaction,
        transfers: List<Transfer>,
        myAddresses: List<String>,
        ticker: TickerData
    ) = let {
        val amount = transfers.map { it.value.toIntOrNull() ?: 0 }.fold(0) { acc, d -> acc + d }.toString()
        TransactionData(
            id = transaction.id,
            date = transaction.registeredAt,
            amount = amount,
            ticker = ticker,
            type = transaction.orderReference?.let { "Outgoing" } ?: "Receive",
            status = transaction.ledgerTransactionData?.ledgerStatus ?: transaction.processing?.status ?: "Unknown",
            price = ValueWithChange(
                Common.computeAmount(amount, ticker.decimals) * ticker.price.value,
                Common.computeAmount(amount, ticker.decimals) * ticker.price.change
            ),
            relatedAccount = getRelatedAccountAddress(domainId, myAddresses, transfers),
        )
    }

    private fun getAddresses(domainId: String, transferParties: List<TransferParty>) = transferParties.flatMap {
        if (it is AddressTransferParty) listOf(it.address)
        else (it as AccountTransferParty).addressDetails?.let { listOf(it.address) } ?: addressRepository.findAll(
            domainId,
            it.accountId
        ).items.map { it.address }
    }

    private fun getRelatedAccountAddress(domainId: String, myAddresses: List<String>, transfers: List<Transfer>) = let {
        val senderAddresses = getAddresses(domainId, transfers.flatMap { it.senders })
        val recipientAddresses = getAddresses(domainId, transfers.mapNotNull { it.recipient })
        senderAddresses.none {
            myAddresses.contains(it)
        }.takeIf { it }?.let {
            senderAddresses.first()
        } ?: recipientAddresses.firstOrNull() ?: "Unknown"
    }
}