package id.walt.gateway

import id.walt.gateway.dto.tickers.TickerData
import id.walt.gateway.providers.metaco.restapi.models.customproperties.TransactionCustomProperties
import java.time.Duration
import java.time.Instant
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

    /**
     * Converts a timeframe string to milliseconds epoch pair
     * @param timeframe - string to represent the timeframe, e.g. 7d for 7 days
     * @return Pair of millisecond epoch, 1st being past, 2nd being now
     */
    fun timeframeToTimestamp(timeframe: String) = Instant.now().let {
        when (timeframe) {
            "1d" -> Pair(it.minus(Duration.ofDays(1)).toEpochMilli(), it.toEpochMilli())
            "7d" -> Pair(it.minus(Duration.ofDays(7)).toEpochMilli(), it.toEpochMilli())
            "3m" -> Pair(it.minus(Duration.ofDays(90)).toEpochMilli(), it.toEpochMilli())
            "6m" -> Pair(it.minus(Duration.ofDays(180)).toEpochMilli(), it.toEpochMilli())
            "1y" -> Pair(it.minus(Duration.ofDays(365)).toEpochMilli(), it.toEpochMilli())
            else -> Pair(it.minus(Duration.ofDays(365)).toEpochMilli(), it.toEpochMilli())
        }
    }
}
