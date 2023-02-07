package id.walt.gateway.dto

import id.walt.gateway.dto.intents.PayloadData

data class CreateAccountPayloadData(
    val alias: String,
    val ledgerId: String,
    val lock: String,
    val keyStrategy: String,
    val vaultId: String,
) : PayloadData
