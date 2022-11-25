package id.walt.gateway.providers.coingecko

interface ResponseParser<T> {
    fun parse(data: String): T
}