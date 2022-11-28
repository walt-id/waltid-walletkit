package id.walt.gateway.usecases

import id.walt.gateway.dto.AssetParameter
import id.walt.gateway.dto.LogoData

interface LogoUseCase {
    fun get(parameter: AssetParameter): LogoData
}