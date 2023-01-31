package id.walt.gateway.usecases

import id.walt.gateway.dto.requests.RequestParameter
import id.walt.gateway.dto.requests.RequestResult

interface RequestUseCase {
    fun create(parameter: RequestParameter, additionalInfo: Map<String, String> = emptyMap()): Result<RequestResult>
    fun validate(parameter: RequestParameter): Result<RequestResult>
}