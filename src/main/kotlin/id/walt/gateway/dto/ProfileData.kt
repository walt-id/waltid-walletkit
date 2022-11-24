package id.walt.gateway.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProfileData(
    val id: String,
    val alias: String? = null,
)
