package id.walt.gateway.dto.trades

import kotlinx.serialization.Serializable

@Serializable
abstract class TradeParameter() {
    abstract val amount: String
    abstract val ticker: String
    abstract val maxFee: String
    abstract val sender: String
}