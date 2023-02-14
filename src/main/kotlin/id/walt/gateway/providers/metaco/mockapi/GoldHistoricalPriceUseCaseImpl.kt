package id.walt.gateway.providers.metaco.mockapi

import id.walt.gateway.Common
import id.walt.gateway.dto.HistoricalPriceData
import id.walt.gateway.dto.HistoricalPriceParameter
import id.walt.gateway.usecases.HistoricalPriceUseCase
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

class GoldHistoricalPriceUseCaseImpl : HistoricalPriceUseCase {
    override fun get(parameter: HistoricalPriceParameter): Result<List<HistoricalPriceData>> = Result.success(
        Common.timeframeToTimestamp(listOf("7d", "3m", "6m", "1y").let {
            it[Common.getRandomInt(0, it.size)]
        }).let {
            val list = mutableListOf<HistoricalPriceData>()
            var date = Instant.ofEpochMilli(it.first).plus(Duration.ofDays(1))
            while (date < Instant.ofEpochMilli(it.second)) {
                list.add(
                    HistoricalPriceData(
                        date = date.truncatedTo(ChronoUnit.SECONDS).toString(),
                        price = Common.getRandomDouble(49.0, 59.0).toString()
                    )
                )
                date = date.plus(Duration.ofDays(1))
            }
            list
        })
}