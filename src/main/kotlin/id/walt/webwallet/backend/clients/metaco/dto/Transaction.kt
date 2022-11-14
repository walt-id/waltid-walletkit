package id.walt.webwallet.backend.clients.metaco.dto

data class Transaction(
    val id: String,
    val isSender: String,
    val value: String,
    val ticker: String,
    val date: String,
    val status: String,
    val account: Account, //related account
)
