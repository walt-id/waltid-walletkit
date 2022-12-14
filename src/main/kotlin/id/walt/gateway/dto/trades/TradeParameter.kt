package id.walt.gateway.dto.trades

data class TradeParameter(
    val amount: String,
    val ticker: String,
    val maxFee: String,
    val sender: String,
    val recipient: String,
)