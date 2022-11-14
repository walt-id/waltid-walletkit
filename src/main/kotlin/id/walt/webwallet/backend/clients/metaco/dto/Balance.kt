package id.walt.webwallet.backend.clients.metaco.dto

data class Balance(
    val ticker: String,
    val price: ValueWithChange,
    val balance: String,
    val value: String,
)
