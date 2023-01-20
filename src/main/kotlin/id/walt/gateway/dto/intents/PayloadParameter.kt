package id.walt.gateway.dto.intents

data class PayloadParameter<T>(
    val type: String,
    val parametersType: String? = null,
    val data: T,
)
