package id.walt.gateway.providers.cryptoiconsapi

import id.walt.gateway.dto.AssetParameter
import id.walt.gateway.dto.LogoData
import id.walt.gateway.usecases.LogoUseCase
import java.net.URLEncoder

class LogoUseCaseImpl : LogoUseCase {
    private val baseUrl = "https://coinicons-api.vercel.app/api/icon/%s"

    override fun get(parameter: AssetParameter): LogoData = LogoData(
        String.format(
            baseUrl,
            URLEncoder.encode(parameter.identifier.lowercase(), "utf-8")
        )
    )
}