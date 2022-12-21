package id.walt.gateway.providers.coingecko

import id.walt.gateway.dto.CoinData

class SimplePriceParser : ResponseParser<CoinData> {
    private val coinRegex = "\"%s\":( *)\\{(.|\n)*},?"
    private val priceRegex = "\"%s\":( *)(\\d+.\\d+),?"
    private val marketCapRegex = "\"%s_market_cap\":( *)(\\d+.\\d+),?"
    private val change24hRegex = "\"%s_24h_change\":( *)(-?\\d+.\\d+),?"

    override fun parse(id: String, currency: String, data: String): CoinData = let {
        val match = Regex(String.format(coinRegex, id)).find(data)
        val coin = match?.groups?.get(0)?.value ?: ""

        val price = Regex(String.format(priceRegex, currency)).find(coin)
        val marketCap = Regex(String.format(marketCapRegex, currency)).find(coin)
        val change24h = Regex(String.format(change24hRegex, currency)).find(coin)

        CoinData(
            price = price?.groups?.get(2)?.value?.toDoubleOrNull() ?: .0,//Double.NaN,
            marketCap = marketCap?.groups?.get(2)?.value?.toDoubleOrNull() ?: .0,//Double.NaN,
            change = change24h?.groups?.get(2)?.value?.toDoubleOrNull() ?: .0,//Double.NaN
        )
    }
}