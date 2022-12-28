package id.walt.gateway.dto.transactions

import id.walt.gateway.dto.AmountWithValue
import id.walt.gateway.dto.TransferData
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class TransactionTransferData(
    val status: String,
    val date: String = LocalDateTime.now().toString(),
    val total: AmountWithValue,
    val transfers: List<TransferData>,
)
