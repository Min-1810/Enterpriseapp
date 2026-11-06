package com.fpt.enterpriseapp.data.model

data class Transaction(
    val id: String = "",
    val contractId: String = "",
    val contractCode: String = "",
    val customerName: String = "",
    val amount: Double = 0.0,
    val paymentDate: Long = System.currentTimeMillis(),
    val status: String = "PENDING",
    val method: String = "BANK_TRANSFER",
    val note: String = ""
)