package id.walt.gateway.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProfileData(
    val id: String,
    val ticker: String,
    val addresses: List<String>,
    val alias: String? = null,
)
