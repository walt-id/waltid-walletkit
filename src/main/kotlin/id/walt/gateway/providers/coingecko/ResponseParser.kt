package id.walt.gateway.providers.coingecko

interface ResponseParser<T> {
    fun parse(id: String, currency: String, data: String): T
}