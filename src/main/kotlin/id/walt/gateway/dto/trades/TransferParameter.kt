package id.walt.gateway.dto.trades

import kotlinx.serialization.Serializable

@Serializable
data class TransferParameter(
    override val amount: String,
    override val ticker: String,
    override val maxFee: String,
    override val sender: String,
    val recipient: String,
) : TradeParameter()
