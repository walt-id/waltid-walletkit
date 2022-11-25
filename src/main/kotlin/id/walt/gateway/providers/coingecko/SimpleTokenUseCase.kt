package id.walt.gateway.providers.coingecko

import id.walt.gateway.dto.TokenData
import id.walt.gateway.dto.TokenParameter
import id.walt.gateway.usecases.TokenUseCase

class SimpleTokenUseCase(
    private val repository: CoinRepository,
    private val parser: ResponseParser<TokenData>,
) : TokenUseCase {

    override fun metadata(parameter: TokenParameter): TokenData = let {
        parser.parse(repository.findById(parameter.id, parameter.currency))
    }
}