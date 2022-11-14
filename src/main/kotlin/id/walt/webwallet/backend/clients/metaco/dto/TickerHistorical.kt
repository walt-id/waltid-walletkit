package id.walt.webwallet.backend.clients.metaco.dto

data class TickerHistorical(
    val symbol: String,
    val timeframe: String, //e.g.1D, 1W, 1M etc.
    val prices: Map<String, String>,
)
