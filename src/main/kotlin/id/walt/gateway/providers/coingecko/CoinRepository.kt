package id.walt.gateway.providers.coingecko

interface CoinRepository {
    fun findById(id: String, currency: String): String
}