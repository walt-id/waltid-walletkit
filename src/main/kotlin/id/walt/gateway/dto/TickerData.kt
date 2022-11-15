package id.walt.gateway.dto

import id.walt.gateway.metaco.dto.ValueWithChange

data class TickerData(
    val name: String,
    val price: ValueWithChange = ValueWithChange("",""),
    val imageUrl: String? = null,
)
