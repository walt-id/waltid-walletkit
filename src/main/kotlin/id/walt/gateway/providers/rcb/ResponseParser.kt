package id.walt.gateway.providers.rcb

interface ResponseParser<T> {
    fun parse(data: String): T
}