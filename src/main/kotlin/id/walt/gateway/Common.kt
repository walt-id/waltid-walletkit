package id.walt.gateway

import kotlin.random.Random

object Common {
    private val charset = listOf(('0'..'9'), ('a'..'z'), ('A'..'Z'))
    fun getRandomString(length: Int, numbers: Boolean = false): String = List(length) {
        charset[if (numbers) 0 else 1].random()
    }.joinToString("")

    fun getRandomDouble(from: Double, to: Double) = Random.nextDouble(from, to)
}