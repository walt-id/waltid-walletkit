package id.walt.gateway.providers.metaco.mockapi

import id.walt.gateway.Common
import id.walt.gateway.dto.TickerData
import id.walt.gateway.dto.TickerParameter
import id.walt.gateway.dto.ValueWithChange
import id.walt.gateway.usecases.TickerUseCase
import java.util.*

class TickerUseCaseImpl : TickerUseCase {
    override fun get(parameter: TickerParameter): Result<TickerData> = Result.success(getTickerData())

    override fun list(currency: String): Result<List<TickerData>> = Result.success((1..5).map { getTickerData() })

    private fun getTickerData() = getTokenTriple().let {
        TickerData(
            id = UUID.randomUUID().toString(),
            name = it.first,
            kind = it.second,
            symbol = it.third,
            chain = Common.getRandomString(10, 1),
            price = getPrice(),
            imageUrl = if (it.third == "eth") "https://cryptologos.cc/logos/ethereum-eth-logo.png" else "https://cryptologos.cc/logos/pax-gold-paxg-logo.png",
            decimals = Common.getRandomInt(12, 18),
            maxFee = Common.getRandomInt(from = 200, to = 1000),
        )
    }

    private fun getTokenTriple() = let {
        val name = listOf("tGOLD", "Stable Coin")[Common.getRandomInt(to = 2)]
        val kind = if (name == "tGOLD") "Contract" else "Native"
        val symbol = if (name == "tGOLD") "tGOLD" else "eth"
        Triple(name, kind, symbol)
    }

    private fun getPrice() =
        ValueWithChange(
            value = Common.getRandomDouble(1.0, 100.0),
            change = Common.getRandomDouble(-30.0, 30.0),
            currency = "eur"
        )
}