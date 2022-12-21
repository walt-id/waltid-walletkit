package id.walt.gateway

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
}