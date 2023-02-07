package id.walt.gateway.routers

import id.walt.gateway.controllers.AccountController
import io.javalin.apibuilder.ApiBuilder
import io.javalin.plugin.openapi.dsl.documented

class AccountRouter(
    private val accountController: AccountController
): Router {
    override fun routes() {
        ApiBuilder.path("accounts") {
            ApiBuilder.post("login", documented(accountController.profileDoc(), accountController::profile))
            ApiBuilder.post("create", documented(accountController.createDoc(), accountController::create))
            ApiBuilder.get("{profileId}/balance", documented(accountController.balanceDoc(), accountController::balance))
            ApiBuilder.get("{accountId}/domain/{domainId}/balance/{tickerId}", documented(accountController.tickerBalanceDoc(), accountController::tickerBalance))
            ApiBuilder.get("{accountId}/domain/{domainId}/transactions", documented(accountController.transactionsDoc(), accountController::transactions))
            ApiBuilder.get("{accountId}/domain/{domainId}/transactions/{transactionId}", documented(accountController.transactionDoc(), accountController::transaction))
        }
    }
}