package id.walt.gateway.controllers

import id.walt.gateway.dto.accounts.AccountIdentifier
import id.walt.gateway.dto.requests.RequestResult
import id.walt.gateway.dto.trades.SwapParameter
import id.walt.gateway.dto.trades.TradeData
import id.walt.gateway.dto.trades.TransferParameter
import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.usecases.TradeUseCase
import io.javalin.http.Context
import io.javalin.http.HttpCode
import io.javalin.plugin.openapi.dsl.document

class TradeController(
    private val tradeUseCase: TradeUseCase,
) {
    fun sell(ctx: Context) {
        val swap = ctx.bodyAsClass<SwapParameter>()
        tradeUseCase.sell(
            TradeData(TransferParameter(
                swap.spend.amount,
                swap.spend.ticker,
                swap.spend.maxFee,
                sender = swap.spend.sender,
                recipient = swap.receive.sender.takeIf { !it.isEmpty() } ?: AccountIdentifier(
                    ProviderConfig.nostroDomainId,
                    ProviderConfig.nostroAccountId
                ),
            ), "Sale"),
            TradeData(TransferParameter(
                swap.receive.amount,
                swap.receive.ticker,
                swap.receive.maxFee,
                sender = swap.receive.sender.takeIf { !it.isEmpty() } ?: AccountIdentifier(
                    ProviderConfig.nostroDomainId,
                    ProviderConfig.nostroAccountId
                ),
                recipient = swap.spend.sender
            ), "Purchase"),
        ).onSuccess {
            ctx.status(it.result.takeIf { it }?.let { HttpCode.OK } ?: HttpCode.NOT_FOUND)
            ctx.json(it)
        }.onFailure {
            ctx.status(HttpCode.NOT_FOUND)
            ctx.json(it)
        }
    }

    fun buy(ctx: Context) {
        val swap = ctx.bodyAsClass<SwapParameter>()
        tradeUseCase.buy(
            TradeData(TransferParameter(
                amount = swap.spend.amount,
                ticker = swap.spend.ticker,
                maxFee = swap.spend.maxFee,
                sender = swap.spend.sender,
                recipient = swap.receive.sender.takeIf { !it.isEmpty() } ?: AccountIdentifier(
                    ProviderConfig.nostroDomainId,
                    ProviderConfig.nostroAccountId
                ),
            ), "Purchase"),
            TradeData(TransferParameter(
                amount = swap.receive.amount,
                ticker = swap.receive.ticker,
                maxFee = swap.receive.maxFee,
                sender = swap.receive.sender.takeIf { !it.isEmpty() } ?: AccountIdentifier(
                    ProviderConfig.nostroDomainId,
                    ProviderConfig.nostroAccountId
                ),
                recipient = swap.spend.sender,
            ), "Sale"),
        ).onSuccess {
            ctx.status(it.result.takeIf { it }?.let { HttpCode.OK } ?: HttpCode.NOT_FOUND)
            ctx.json(it)
        }.onFailure {
            ctx.status(HttpCode.NOT_FOUND)
            ctx.json(it)
        }
    }

    fun send(ctx: Context) {
        val parameters = ctx.bodyAsClass<TransferParameter>()
        tradeUseCase.send(TradeData(parameters, "Transfer"))
            .onSuccess {
                ctx.status(it.result.takeIf { it }?.let { HttpCode.OK } ?: HttpCode.NOT_FOUND)
                ctx.json(it)
            }.onFailure {
                ctx.status(HttpCode.NOT_FOUND)
                ctx.json(it)
            }
    }

    fun validate(ctx: Context) {
        val parameters = ctx.bodyAsClass<TransferParameter>()
        tradeUseCase.validate(TradeData(TransferParameter(
            amount = parameters.amount,
            ticker = parameters.ticker,
            maxFee = parameters.maxFee,
            sender = parameters.sender,
            recipient = AccountIdentifier(
                domainId = parameters.recipient.domainId.takeIf { it.isNotEmpty() }?:ProviderConfig.nostroDomainId,
                accountId = parameters.recipient.accountId.takeIf { it.isNotEmpty() }?:ProviderConfig.nostroAccountId,
            )),"Transfer"))
            .onSuccess {
                ctx.status(it.result.takeIf { it }?.let { HttpCode.OK } ?: HttpCode.NOT_FOUND)
                ctx.json(it)
            }.onFailure {
                ctx.status(HttpCode.NOT_FOUND)
                ctx.json(it)
            }
    }

    fun sellDocs() = document().operation {
        it.summary("Returns the sell trade details").operationId("sell").addTagsItem("Trade Management")
    }.body<SwapParameter> {
        it.description("Sell parameters:<br/>" +
        "{<br/>" +
        "    \"spend\":<br/>" +
        "    {<br/>" +
        "        \"amount\": \"{amount}\",<br/>" +
        "        \"ticker\": \"{ticker-id}\",<br/>" +
        "        \"maxFee\": \"{maxFee}\",<br/>" +
        "        \"sender\":<br/>" +
        "        {<br/>" +
        "            \"domainId\": \"{domain-id}\",<br/>" +
        "            \"accountId\": \"{account-id}\"<br/>" +
        "        }<br/>" +
        "    },<br/>" +
        "    \"receive\":<br/>" +
        "    {<br/>" +
        "        \"amount\": \"{amount}\",<br/>" +
        "        \"ticker\": \"{ticker-id}\",<br/>" +
        "        \"maxFee\": \"{maxFee}\",<br/>" +
        "        \"sender\":<br/>" +
        "        {<br/>" +
        "            \"domainId\": \"{domain-id | empty-string}\",<br/>" +
        "            \"accountId\": \"{account-id | address}\"<br/>" +
        "        }<br/>" +
        "    }<br/>" +
        "}")
    }.json<RequestResult>("200") { it.description("The sell trade details") }

    fun buyDocs() = document().operation {
        it.summary("Returns the buy trade details").operationId("buy").addTagsItem("Trade Management")
    }.body<SwapParameter> {
        it.description("Buy parameters:<br/>" +
        "{<br/>" +
        "    \"spend\":<br/>" +
        "    {<br/>" +
        "        \"amount\": \"{amount}\",<br/>" +
        "        \"ticker\": \"{ticker-id}\",<br/>" +
        "        \"maxFee\": \"{maxFee}\",<br/>" +
        "        \"sender\":<br/>" +
        "        {<br/>" +
        "            \"domainId\": \"{domain-id}\",<br/>" +
        "            \"accountId\": \"{account-id}\"<br/>" +
        "        }<br/>" +
        "    },<br/>" +
        "    \"receive\":<br/>" +
        "    {<br/>" +
        "        \"amount\": \"{amount}\",<br/>" +
        "        \"ticker\": \"{ticker-id}\",<br/>" +
        "        \"maxFee\": \"{maxFee}\",<br/>" +
        "        \"sender\":<br/>" +
        "        {<br/>" +
        "            \"domainId\": \"{domain-id | empty-string}\",<br/>" +
        "            \"accountId\": \"{account-id | address}\"<br/>" +
        "        }<br/>" +
        "    }<br/>" +
        "}")
    }.json<RequestResult>("200") { it.description("The buy trade details") }

    fun sendDocs() = document().operation {
        it.summary("Returns the send trade details").operationId("send").addTagsItem("Trade Management")
    }.body<TransferParameter> {
        it.description("Send parameters:<br/>" +
                "{<br/>" +
                "\"amount\": \"{value}\",<br/>" +
                "\"ticker\": \"{ticker-id}\",<br/>" +
                "\"maxFee\": \"{value}\",<br/>" +
                "\"sender\": {<br/>" +
                "\"domainId\": \"{domain-id}\",<br/>" +
                "\"accountId\": \"{account-id}\"<br/>" +
                "},<br/>" +
                "\"recipient\": {<br/>" +
                "\"domainId\": \"{domain-id}\",<br/>" +
                "\"accountId\": \"{account-id}\"<br/>" +
                "}<br/>" +
                "}")
    }.json<RequestResult>("200") { it.description("The send trade details") }

    fun validateDocs() = document().operation {
        it.summary("Returns the trade validation details").operationId("validate").addTagsItem("Trade Management")
    }.body<TransferParameter> {
        it.description("Trade preview parameters:<br/>" +
                "{<br/>" +
                "\"amount\": \"{value}\",<br/>" +
                "\"ticker\": \"{ticker-id}\",<br/>" +
                "\"maxFee\": \"{value}\",<br/>" +
                "\"sender\": {<br/>" +
                "\"domain-id\": \"{domain-id}\",<br/>" +
                "\"account-id\": \"{account-id}\"<br/>" +
                "},<br/>" +
                "\"recipient\": {<br/>" +
                "\"domainId\": \"{domain-id}\",<br/>" +
                "\"accountId\": \"{account-id}\"<br/>" +
                "}<br/>" +
                "}")
    }.json<RequestResult>("200") { it.description("The trade validation details") }
}
