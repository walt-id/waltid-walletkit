package id.walt.webwallet.backend.config

import com.fasterxml.jackson.annotation.JsonIgnore
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata
import id.walt.verifier.backend.WalletConfiguration
import id.walt.webwallet.backend.wallet.WalletController
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

data class IssuerConfiguration (
  val id: String,
  val url: String,
  val description: String,
  val client_id: String? = null,
  val client_secret: String? = null
) {

  @JsonIgnore
  var metadata: OIDCProviderMetadata? = null
    get() {
      if(field == null) {
        val client = HttpClient.newBuilder().build();
        val request = HttpRequest.newBuilder()
          .uri(URI.create("${url.trimEnd('/')}/.well-known/openid-configuration"))
          .GET()
          .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString());
        val body = response.body()
        metadata = OIDCProviderMetadata.parse(body)
      }
      return field
    }
    private set

  companion object {
    fun getDefaultIssuerConfigurations(): Map<String, IssuerConfiguration> {
      return mapOf(
        Pair("walt.id", IssuerConfiguration("walt.id", "http://localhost:5000/issuer-api/oidc", "walt.id Issuer Portal"))
      )
    }
  }
}
