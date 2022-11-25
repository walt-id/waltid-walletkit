package id.walt.gateway.providers.coingecko

import id.walt.gateway.dto.TokenData
import id.walt.gateway.dto.TokenParameter

class SimplePriceParser(
    private val parameters: TokenParameter,
) : ResponseParser<TokenData> {
    private val coinRegex = "\"%s\":( *)\\{(.|\n)*},?"
    private val priceRegex = "\"%s\":( *)(\\d+.\\d+),?"
    private val marketCapRegex = "\"%s_market_cap\":( *)(\\d+.\\d+),?"
    private val change24hRegex = "\"%s_24h_change\":( *)(-?\\d+.\\d+),?"

    override fun parse(data: String): TokenData = let {
        val match = Regex(String.format(coinRegex, parameters.id)).find(data)
        val coin = match?.groups?.get(0)?.value ?: ""

        val price = Regex(String.format(priceRegex, parameters.currency)).find(coin)
        val marketCap = Regex(String.format(marketCapRegex, parameters.currency)).find(coin)
        val change24h = Regex(String.format(change24hRegex, parameters.currency)).find(coin)

        TokenData(
            price = price?.groups?.get(2)?.value ?: "",
            marketCap = marketCap?.groups?.get(2)?.value ?: "",
            change = change24h?.groups?.get(2)?.value ?: ""
        )
    }
}