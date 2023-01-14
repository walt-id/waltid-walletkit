package id.walt.gateway.providers.rcb

import com.beust.klaxon.Klaxon
import id.walt.gateway.dto.coins.CoinData

class StringFieldResponseParser : ResponseParser<CoinData> {

    override fun parse(data: String): CoinData = let {
        Klaxon().parse<Map<String, String>>(data)?.let {
            CoinData(
                price = it["price"]?.toDoubleOrNull() ?: throw Exception("Could not parse field price"),
                marketCap = it["marketCap"]?.toDoubleOrNull() ?: throw Exception("Could not parse field price"),
                change = it["change"]?.toDoubleOrNull() ?: throw Exception("Could not parse field price"),
            )
        } ?: throw Exception("Could not parse $data")
    }
}