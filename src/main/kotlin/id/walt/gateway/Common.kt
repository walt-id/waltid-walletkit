package id.walt.gateway

import id.walt.gateway.dto.tickers.TickerData
import id.walt.gateway.providers.metaco.restapi.models.customproperties.TransactionCustomProperties
import kotlin.random.Random

object Common {
    private val charset = listOf(('0'..'9'), ('a'..'z'), ('A'..'Z'))
    fun getRandomString(length: Int, letters: Int = 0): String = List(length) {
        charset[when (letters) {
            0 -> 0
            1 -> getRandomInt(1, 3)
            else -> getRandomInt(0, 3)
        }].random()
    }.joinToString("")

    fun getRandomDouble(from: Double, to: Double) = Random.nextDouble(from, to)

    fun getRandomLong(from: Long = 1, to: Long) = Random.nextLong(from, to)

    fun getRandomInt(from: Int = 0, to: Int) = Random.nextInt(from, to)

    fun computeAmount(amount: String, decimals: Int): Double = StringBuilder(
        if (amount.length < decimals) {
            amount.padStart(decimals, '0')
        } else {
            amount
        }
    ).let {
        it.insert(it.length - decimals, '.')
    }.toString().toDoubleOrNull() ?: .0//Double.NaN

    fun getTransactionMeta(tradeType: String, amount: String, ticker: TickerData): TransactionCustomProperties =
        when (tradeType) {
            "Outgoing", "Purchase" -> ticker.askPrice
            "Receive", "Sale" -> ticker.bidPrice
            else -> ticker.price
        }.let {
            TransactionCustomProperties(
                value = (computeAmount(amount, ticker.decimals) * it.value).toString(),
                change = (computeAmount(amount, ticker.decimals) * it.change).toString(),
                currency = it.currency,
                tokenPrice = it.value.toString(),
                tokenSymbol = ticker.symbol,
                tokenDecimals = ticker.decimals.toString(),
                type = tradeType,
            )
        }
}