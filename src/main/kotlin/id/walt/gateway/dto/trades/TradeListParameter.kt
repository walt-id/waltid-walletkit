package id.walt.gateway.dto.trades

data class TradeListParameter(
    val domainId: String,
    val accountId: String,
    val tickerId: String? = null,
)
