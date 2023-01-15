package id.walt.gateway.dto.balances

import id.walt.gateway.dto.ValueWithChange

data class AccountBalance(
    val balances: List<BalanceData>
) {
    val total = balances.map {
        Triple(it.price.value, it.price.change, it.price.currency)
    }.fold(Triple(.0, .0, "*")) { acc, d ->
        //TODO: balances can have multiple currencies, need to recalculate each balance for a specified currency
        // currently the last balance currency is taken as the actual one
        Triple(acc.first.plus(d.first), acc.second.plus(d.second), d.third)
    }.let {
        ValueWithChange(it.first, it.second, it.third)
    }
}
