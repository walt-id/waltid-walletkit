package id.walt.gateway.controllers

import id.walt.gateway.dto.accounts.AccountInitiationParameter
import id.walt.gateway.dto.balances.AccountBalance
import id.walt.gateway.dto.balances.BalanceData
import id.walt.gateway.dto.balances.BalanceParameter
import id.walt.gateway.dto.profiles.ProfileData
import id.walt.gateway.dto.profiles.ProfileParameter
import id.walt.gateway.dto.requests.RequestResult
import id.walt.gateway.dto.transactions.TransactionData
import id.walt.gateway.dto.transactions.TransactionListParameter
import id.walt.gateway.dto.transactions.TransactionParameter
import id.walt.gateway.dto.transactions.TransactionTransferData
import id.walt.gateway.usecases.AccountUseCase
import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document

class AccountController(
    private val accountUseCase: AccountUseCase,
) {
    fun profile(ctx: Context) {
        val profile = ctx.bodyAsClass<ProfileParameter>()
        accountUseCase.profile(profile)
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun create(ctx: Context) {
        val parameter = ctx.bodyAsClass<AccountInitiationParameter>()
        accountUseCase.create(parameter)
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun createBulk(ctx: Context) {
        val ledgerId = ctx.pathParam("ledgerId")
        ctx.uploadedFiles().map {
            it.content.bufferedReader().use { it.readText() }.split(Regex("[\n\r]+"))
                .fold(listOf<Result<RequestResult>>()) { acc, res ->
                    val tokens = res.split(",")
                    acc.plus(accountUseCase.create(AccountInitiationParameter(tokens[0], tokens[1], ledgerId)))
                }
        }.flatten().run {
            ctx.json(this.joinToString("\n"))
        }
    }

    fun list(ctx: Context) {
        ctx.json(accountUseCase.list().getOrNull()?.joinToString("\n") {
            "${it.domainName},${it.accountAlias},${it.address.joinToString(":")}"
        } ?: "No accounts")
    }

    fun balance(ctx: Context) {
        val profileId = ctx.pathParam("profileId")
        accountUseCase.balance(ProfileParameter(profileId))
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun tickerBalance(ctx: Context) {
        val accountId = ctx.pathParam("accountId")
        val domainId = ctx.pathParam("domainId")
        val tickerId = ctx.pathParam("tickerId")
        accountUseCase.balance(BalanceParameter(domainId, accountId, tickerId))
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun transactions(ctx: Context) {
        val accountId = ctx.pathParam("accountId")
        val domainId = ctx.pathParam("domainId")
        val tickerId = ctx.queryParam("tickerId")
        accountUseCase.transactions(TransactionListParameter(domainId, accountId, tickerId))
            .onSuccess {
                ctx.json(it)
            }.onFailure {
                ctx.json(it)
            }
    }

    fun transaction(ctx: Context) {
        val accountId = ctx.pathParam("accountId")
        val domainId = ctx.pathParam("domainId")
        val transactionId = ctx.pathParam("transactionId")
        accountUseCase.transaction(
            TransactionParameter(domainId, accountId, transactionId)
        ).onSuccess {
            ctx.json(it)
        }.onFailure {
            ctx.json(it)
        }
    }

    fun profileDoc() = document().operation {
        it.summary("Returns the account profile data").operationId("profile").addTagsItem("Account Management")
    }.body<ProfileParameter> {
        it.description(
            "Profile parameter.<br/>" +
                    "{<br/>" +
                    "\"id\": \"{login-data}\"<br/>" +
                    "}"
        )
    }.json<ProfileData>("200") { it.description("The account profile data") }

    fun createDoc() = document().operation {
        it.summary("Creates an account having the specified alias in the specified domain, on the specified ledger")
            .operationId("create").addTagsItem("Account Management")
    }.body<ProfileParameter> {
        it.description(
            "Account initiation parameter.<br/>" +
                    "{<br/>" +
                    "\"domainName\": \"{domain-name}\",<br/>" +
                    "\"accountName\": \"{account-name}\",<br/>" +
                    "\"ledgerId\": \"{ledger-id}\"<br/>" +
                    "}"
        )
    }.json<RequestResult>("200") { it.description("The account initiation request result") }

    fun createBulkDoc() = document().operation {
        it.summary("Creates accounts from file").operationId("createBulk").addTagsItem("Account Management")
    }.uploadedFile("file") {
        it.description = "File"
        it.required = true
    }.json<RequestResult>("200") { it.description("The account initiation request result") }

    fun listDoc() = document().operation {
        it.summary("Returns the list of all available accounts").operationId("list").addTagsItem("Account Management")
    }.json<String>("200") { it.description("The list of account basic data") }

    fun balanceDoc() = document().operation {
        it.summary("Returns the account balance").operationId("balance").addTagsItem("Account Management")
    }.json<AccountBalance>("200") { it.description("The account balance") }

    fun tickerBalanceDoc() = document().operation {
        it.summary("Returns the account balance for ticker").operationId("tickerBalance")
            .addTagsItem("Account Management")
    }.json<BalanceData>("200") { it.description("The account balance for ticker") }

    fun transactionsDoc() = document().operation {
        it.summary("Returns the account transactions").operationId("transactions").addTagsItem("Account Management")
    }.queryParam<String>("tickerId").json<List<TransactionData>>("200") { it.description("The account transactions") }

    fun transactionDoc() = document().operation {
        it.summary("Returns the transaction transfers").operationId("transaction").addTagsItem("Account Management")
    }.json<TransactionTransferData>("200") { it.description("The transaction transfers") }

}
