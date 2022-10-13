package id.walt.webwallet.backend.config

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import id.walt.model.oidc.OIDCProvider
import java.io.File
import java.nio.charset.StandardCharsets

@Target(AnnotationTarget.FIELD)
annotation class ExternalHostnameUrl

fun getExternalHostname(): String? {
    return System.getenv("EXTERNAL_HOSTNAME")
        ?: System.getenv("HOSTNAMEE") // linux
        ?: File("/etc/hostname").let { file -> // linux alternative
            if (file.exists()) {
                file.readText(StandardCharsets.UTF_8).trim()
            } else {
                null
            }
        }
        ?: System.getenv("COMPUTERNAME") // windows
}

fun replaceExternalHostname(url: String): String = getExternalHostname()?.let { url.replace("\$EXTERNAL_HOSTNAME", it) } ?: url

val externalHostnameUrlValueConverter = object : Converter {
    override fun canConvert(cls: Class<*>) = cls == String::class.java

    override fun fromJson(jv: JsonValue) =
        jv.string?.let { replaceExternalHostname(it) }

    override fun toJson(value: Any) = Klaxon().toJsonString(value)
}

fun OIDCProvider.withExternalHostnameUrl(): OIDCProvider =
    OIDCProvider(this.id, replaceExternalHostname(this.url), this.description, this.client_id, this.client_secret)
