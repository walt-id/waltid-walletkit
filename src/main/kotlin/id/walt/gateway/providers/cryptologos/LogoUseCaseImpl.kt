package id.walt.gateway.providers.cryptologos

import id.walt.gateway.dto.AssetParameter
import id.walt.gateway.dto.LogoData
import id.walt.gateway.usecases.LogoUseCase
import java.net.URLEncoder

class LogoUseCaseImpl : LogoUseCase {
    private val baseUrl = "https://cryptologos.cc/logos/%s-%s-logo.png"

    override fun get(parameter: AssetParameter): LogoData =
        LogoData(URLEncoder.encode(String.format(baseUrl, parameter.chain, parameter.identifier), "utf-8"))
}