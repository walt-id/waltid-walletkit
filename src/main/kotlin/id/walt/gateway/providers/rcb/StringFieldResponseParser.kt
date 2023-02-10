package id.walt.gateway.providers.rcb

import com.beust.klaxon.Klaxon
import id.walt.gateway.dto.coins.CoinData

class StringFieldResponseParser : ResponseParser<CoinData> {

    override fun parse(data: String): CoinData = let {
        Klaxon().parse<Map<String, String>>(data)?.let {
            CoinData(
                askPrice = it["askprice"]?.toDoubleOrNull() ?: throw Exception("Could not parse field askprice"),
                bidPrice = it["bidprice"]?.toDoubleOrNull() ?: throw Exception("Could not parse field bidprice"),
                marketCap = it["marketCap"]?.toDoubleOrNull() ?: .0,
                change = it["change"]?.toDoubleOrNull() ?: .0,
            )
        } ?: throw Exception("Could not parse $data")
    }
}