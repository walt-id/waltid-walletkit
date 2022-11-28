package id.walt.providers.coingecko

import id.walt.gateway.dto.CoinData
import id.walt.gateway.dto.CoinParameter
import id.walt.gateway.providers.coingecko.CoinRepository
import id.walt.gateway.providers.coingecko.ResponseParser
import id.walt.gateway.providers.coingecko.SimpleCoinUseCase
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class UseCaseTest : StringSpec({

    val data = "{\n" +
            "  \"bitcoin\": {\n" +
            "    \"eur\": 15901.82,\n" +
            "    \"eur_market_cap\": 305246870774.20654,\n" +
            "    \"eur_24h_change\": 0.36233145138594613\n" +
            "  },\n" +
            "  \"matic-network\": {\n" +
            "    \"eur\": 0.810057,\n" +
            "    \"eur_market_cap\": 7177541018.584296,\n" +
            "    \"eur_24h_change\": -0.5512113103483903\n" +
            "  }\n" +
            "}"


    val parser = mockk<ResponseParser<CoinData>>()
    val repository = mockk<CoinRepository>()

    "simple token use case returns the parsed value" {
        forAll(
            row(CoinParameter("bitcoin", "eur"), CoinData(15901.82, 305246870774.20654, 0.36233145138594613)),
            row(CoinParameter("matic-network", "eur"), CoinData(0.810057, 7177541018.584296, -0.5512113103483903)),
        ) { param, result ->
            // given
            every { repository.findById(any(), any()) } returns data
            every { parser.parse(data) } returns result

            // when
            val response = SimpleCoinUseCase(repository, parser).metadata(param)

            // then
            response shouldBe result
        }
    }
})