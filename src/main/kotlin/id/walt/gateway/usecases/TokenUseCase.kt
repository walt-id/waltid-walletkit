package id.walt.gateway.usecases

import id.walt.gateway.dto.TokenData
import id.walt.gateway.dto.TokenParameter

interface TokenUseCase {
    fun metadata(parameter: TokenParameter): TokenData
}