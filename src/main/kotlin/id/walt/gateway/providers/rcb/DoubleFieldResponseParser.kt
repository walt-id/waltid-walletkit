package id.walt.gateway.providers.rcb

import com.beust.klaxon.Klaxon
import id.walt.gateway.dto.CoinData

class DoubleFieldResponseParser: ResponseParser<CoinData> {
    override fun parse(data: String): CoinData = let {
        Klaxon().parse<CoinData>(data) ?: throw Exception("Could not parse $data")
    }
}