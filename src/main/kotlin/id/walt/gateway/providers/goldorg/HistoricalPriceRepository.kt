package id.walt.gateway.providers.goldorg

interface HistoricalPriceRepository {
    fun get(timeframe: String): Map<String, String>
}