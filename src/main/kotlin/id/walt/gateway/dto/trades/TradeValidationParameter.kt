package id.walt.gateway.dto.trades

data class TradeValidationParameter(
    val domainId: String,
    val trade: TradePreview,
)
