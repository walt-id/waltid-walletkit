package id.walt.webwallet.backend.config

import com.beust.klaxon.Converter
import com.beust.klaxon.Json
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import id.walt.model.oidc.OIDCProvider

@Target(AnnotationTarget.FIELD)
annotation class ExternalHostnameUrl

fun replaceExternalHostname(url: String): String = url.replace("\$EXTERNAL_HOSTNAME", System.getenv("EXTERNAL_HOSTNAME") ?: System.getenv("HOSTNAME"))

val externalHostnameUrlValueConverter = object: Converter {
  override fun canConvert(cls: Class<*>) = cls == String::class.java

  override fun fromJson(jv: JsonValue) =
      jv.string?.let { replaceExternalHostname(it) }

  override fun toJson(value: Any)
      = Klaxon().toJsonString(value)
}

fun OIDCProvider.withExternalHostnameUrl(): OIDCProvider =
  OIDCProvider(this.id, replaceExternalHostname(this.url), this.description, this.client_id, this.client_secret)
