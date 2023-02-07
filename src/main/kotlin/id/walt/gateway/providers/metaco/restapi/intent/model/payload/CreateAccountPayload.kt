package id.walt.gateway.providers.metaco.restapi.intent.model.payload

import kotlinx.serialization.Serializable

@Serializable
data class CreateAccountPayload(
    val id: String,
    val alias: String,
    val providerDetails: CreateAccountProviderDetailsPayload,
    val ledgerId: String,
    val lock: String,
    val customProperties: Map<String, String> = emptyMap(),
) : Payload(Types.CreateAccount.value) {
    @Serializable
    data class CreateAccountProviderDetailsPayload(
        val vaultId: String,
        val keyStrategy: String,
        val type: String = "Vault",
    )
}
