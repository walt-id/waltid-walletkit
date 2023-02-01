package id.walt.gateway.dto

import id.walt.gateway.dto.intents.PayloadData

data class QuarantineTransferPayloadData(
    val accountId: String,
    val transfers: List<String>,
) : PayloadData
