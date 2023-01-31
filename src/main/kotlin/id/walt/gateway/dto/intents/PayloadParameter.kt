package id.walt.gateway.dto.intents

data class PayloadParameter<T>(
    val type: String,
    val data: T,
    val parametersType: String = "",
    val additionalInfo: Map<String, String> = emptyMap(),
)
