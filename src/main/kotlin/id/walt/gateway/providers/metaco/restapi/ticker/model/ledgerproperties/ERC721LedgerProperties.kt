package id.walt.gateway.providers.metaco.restapi.ticker.model.ledgerproperties

class ERC721LedgerProperties(
    val address: String,
    val tokenId: String,
) : LedgerProperties("ERC721") {
}