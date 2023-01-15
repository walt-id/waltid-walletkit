package id.walt.gateway.dto.trades

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeParameter(
    val spend: TradeParameter,
    val receive: TradeParameter,
)