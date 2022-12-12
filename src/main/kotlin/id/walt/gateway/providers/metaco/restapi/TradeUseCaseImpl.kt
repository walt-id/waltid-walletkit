package id.walt.gateway.providers.metaco.restapi

import id.walt.gateway.dto.trades.*
import id.walt.gateway.providers.metaco.repositories.IntentRepository
import id.walt.gateway.providers.metaco.restapi.intent.builders.IntentBuilder
import id.walt.gateway.providers.metaco.restapi.intent.builders.ParameterBuilder
import id.walt.gateway.providers.metaco.restapi.intent.builders.TransactionOrderIntentBuilder
import id.walt.gateway.providers.metaco.restapi.intent.model.CustomProperties
import id.walt.gateway.providers.metaco.restapi.intent.model.estimate.EthereumEstimate
import id.walt.gateway.providers.metaco.restapi.intent.model.payload.TransactionOrderPayload
import id.walt.gateway.usecases.TradeUseCase
import java.util.*

class TradeUseCaseImpl(
    private val intentRepository: IntentRepository,
) : TradeUseCase {
    override fun sell(parameter: SellParameter): Result<SellData> {
        TODO("Not yet implemented")
    }

    override fun buy(parameter: BuyParameter): Result<BuyData> {
        TODO("Not yet implemented")
    }

    override fun send(parameter: SendParameter): Result<SendData> {
        TODO("Not yet implemented")
    }

    override fun validate(parameter: TradeValidationParameter): Result<TradeValidationData> = runCatching {
        //TODO: don't use model here
        TransactionOrderPayload(
            id = UUID.randomUUID().toString(),
            accountId = parameter.trade.sender,
            customProperties = CustomProperties(),
            parameters = ParameterBuilder.getBuilder(parameter.trade.ticker).build(parameter.trade)
        ).run {
            intentRepository.validate(parameter.domainId, this).let {
                TradeValidationData(
                    result = it.result.type == "Success",
                    message = it.result.reason //+ (it.estimate as EthereumEstimate)?.gas.let { " (gas: $it)" }
                )
            }
        }
    }
}