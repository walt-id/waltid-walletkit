package id.walt.gateway.providers.metaco.restapi.models.customproperties

data class TransactionCustomProperties(
    val value: String,
    val change: String,
    val currency: String,
    val tokenPrice: String,
    val tokenSymbol: String,
    val tokenDecimals: String,
    val type: String,
)

fun TransactionCustomProperties.toMap() = mapOf(
    "value" to this.value,
    "change" to this.change,
    "currency" to this.currency,
    "tokenPrice" to this.tokenPrice,
    "tokenSymbol" to this.tokenSymbol,
    "tokenDecimals" to this.tokenDecimals,
    "type" to this.type,
)
